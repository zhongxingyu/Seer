 /*
  * Copyright 2010, 2011 mapsforge.org
  *
  * This program is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mapsforge.applications.debug;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 
 import org.mapsforge.core.MercatorProjection;
 
 /**
  * Class for reading the current map format.
  * 
  * @author Karsten
  * 
  */
 public class MapFormatReader {
 	private static final String HEXES = "0123456789ABCDEF";
 
 	private RandomAccessFile f;
 	private byte[] buffer;
 	private long offset = 0;
 	/** for switching between a long and double representation of longitude and latitude values */
 	private static final double COORDINATES_FACTOR = 1000000.0;
 
 	private MapFile mapFile;
 
 	private byte[] magicByte;
 	private int headerSize;
 	private int fileVersion;
 	// TODO store value in MapFile
 	private byte flags;
 	private byte amountOfZoomIntervals;
 	private String projection;
 	private int tileSize;
 
 	// Bounding box
 	private int maxLat;
 	private int minLon;
 	private int minLat;
 	private int maxLon;
 
 	// Map start position
 	private int mapStartLon;
 	private int mapStartLat;
 	private long dateOfCreation;
 
 	// POI tag mapping
 	private int numPOIMappings;
 
 	// Ways tag mapping
 	private int numWayTagMappings;
 
 	private String comment;
 
 	// Zoom interval configuration
 	private byte[] baseZoomLevel;
 	private byte[] minimalZoomLevel;
 	private byte[] maximalZoomLevel;
 	private long[] absoluteStartPosition;
 	private long[] subFileSize;
 	private ArrayList<Long>[] tileOffsets;
 
 	/**
 	 * The constructor.
 	 * 
 	 * @param path
 	 *            Path to the map file that should be read.
 	 * @throws FileNotFoundException
 	 *             when file cannot be found.
 	 */
 	public MapFormatReader(String path) throws FileNotFoundException {
 		this.f = new RandomAccessFile(path, "r");
 		// this.f = new RAF(path, "r");
 
 		this.buffer = new byte[1024 * 100];
 		this.mapFile = new MapFile();
 	}
 
 	/**
 	 * Parses the map file and stores its structure.
 	 * 
 	 * @throws IOException
 	 *             when the file cannot be read.
 	 */
 	public MapFile parseFile() throws IOException {
 
 		parseHeader();
 
 		this.tileOffsets = new ArrayList[this.mapFile.getAmountOfZoomIntervals()];
 
 		SubFile sf;
 		for (byte i = 0; i < this.mapFile.getAmountOfZoomIntervals(); i++) {
 			// TODO add subfile to map file
 			this.mapFile.addSubFile(getNextSubFile(i));
 		}
 
 		System.out.println("####DONE");
 
 		return this.mapFile;
 	}
 
 	/**
 	 * Closes the file handler.
 	 * 
 	 * @throws IOException
 	 *             when your disk is going to die.
 	 */
 	public void close() throws IOException {
 		if (this.f != null)
 			this.f.close();
 	}
 
 	private void parseHeader() throws IOException {
 		System.out.println("Parsing header...");
 		// Magic bytes (20B)
 		this.magicByte = new byte[20];
 		this.f.read(this.magicByte, 0, 20);
 		this.offset += 20;
 		this.mapFile.setMagicByte(this.magicByte);
 
 		// Header size (4B)
 		this.mapFile.setHeaderSize(getNextInt());
 
 		// File version (4B)
 		this.mapFile.setFileVersion(getNextInt());
 
 		// Flags (1B)
 		this.mapFile.setFlags(getNextByte());
 
 		// Number of zoom intervals (1B)
 		this.mapFile.setAmountOfZoomIntervals(getNextByte());
 
 		// Projection name (variable)
 		this.mapFile.setProjection(getNextString());
 
 		// Tile size (2B)
 		this.mapFile.setTileSize(getNextDword());
 
 		// Bounding box (4*4B)
 		this.mapFile.setBoundingBox(getNextInt(), getNextInt(), getNextInt(), getNextInt());
 
 		// Map start position (8B)
 		if (this.mapFile.isMapStartPositionFlagSet()) {
 			this.mapFile.setMapStartPosition(getNextInt(), getNextInt());
 		}
 
 		// Date of creation (8B)
 		this.mapFile.setDateOfCreation(getNextLong());
 
 		// POI tag mapping (variable)
 		// amount of mappings (2B)
 		this.mapFile.setAmountOfPOIMappings(getNextDword());
 		this.mapFile.preparePOIMappings();
 
 		// POI tag mapping (variable)
 		int tagID;
 		String tagName;
 		for (int i = 0; i < this.mapFile.getAmountOfPOIMappings(); i++) {
 
 			// Tag name (variable)
 			tagName = getNextString();
 
 			// tag ID (2B)
 			tagID = getNextDword();
 			this.mapFile.getPOIMappings()[tagID] = tagName;
 		}
 
 		// Way tag mapping (variable)
 		// amount of mappings (2B)
 		this.mapFile.setAmountOfWayTagMappings(getNextDword());
 		this.mapFile.prepareWayTagMappings();
 
 		// Way tag mapping (variable)
 		for (int i = 0; i < this.mapFile.getAmountOfWayTagMappings(); i++) {
 			// tag name (variable)
 			tagName = getNextString();
 
 			// tag ID (2B)
 			tagID = getNextDword();
 			this.mapFile.getWayTagMappings()[tagID] = tagName;
 		}
 
 		// Comment (variable)
 		this.mapFile.setComment(getNextString());
 
 		System.out.println("Index offset offset: " + this.offset);
 
 		// Zoom interval configuration (variable)
 		this.mapFile.prepareZoomIntervalConfiguration();
 		for (int i = 0; i < this.mapFile.getAmountOfZoomIntervals(); i++) {
 			this.mapFile.setZoomIntervalConfiguration(i, getNextByte(), getNextByte(), getNextByte(),
 					getNextLong5(), getNextLong5());
 		}
 
 	}
 
 	private SubFile getNextSubFile(byte zoomInterval) throws IOException {
 		long indexOffset;
 		System.out.println("--- S U B F I L E " + zoomInterval + " ---");
 		SubFile sf = new SubFile(this.mapFile, zoomInterval);
 		this.mapFile.addSubFile(sf);
 
 		this.offset = this.mapFile.getAbsoluteStartPosition()[zoomInterval];
 		this.f.seek(this.offset);
 
 		System.out.println("  Subfile start offset: " + this.offset + " (" + getHex(this.offset) + ")");
 
 		// Calculate number of blocks in the file index
 		// x,y tile coordinates of the first and last tile in the index
 		long firstX = MercatorProjection.longitudeToTileX(this.mapFile.getMinLon()
 				/ MapFormatReader.COORDINATES_FACTOR, this.mapFile.getBaseZoomLevel()[zoomInterval]);
 		long lastX = MercatorProjection.longitudeToTileX(this.mapFile.getMaxLon()
 				/ MapFormatReader.COORDINATES_FACTOR, this.mapFile.getBaseZoomLevel()[zoomInterval]);
 		long firstY = MercatorProjection.latitudeToTileY(this.mapFile.getMinLat()
 				/ MapFormatReader.COORDINATES_FACTOR, this.mapFile.getBaseZoomLevel()[zoomInterval]);
 		long lastY = MercatorProjection.latitudeToTileY(this.mapFile.getMaxLat()
 				/ MapFormatReader.COORDINATES_FACTOR, this.mapFile.getBaseZoomLevel()[zoomInterval]);
 
 		// System.out.println("First x: " + firstX);
 		// System.out.println("Last x: " + lastX);
 		// System.out.println("First y: " + firstY);
 		// System.out.println("Last y: " + lastY);
 		// System.out.println("Base zoom level: " + this.mapFile.getBaseZoomLevel()[zoomInterval]);
 
 		long numBlocks = (Math.abs(lastX - firstX) + 1) * (Math.abs(lastY - firstY) + 1);
 		System.out.println("  Number of tiles (blocks) in this subfile: " + numBlocks);
 
 		// Save the index offset (for debug purposes)
 		indexOffset = this.offset;
 		System.out.println("Index offset: " + this.offset);
 
 		// Tile index segment
 		// index signature (16B, optional)
 		if (this.mapFile.isDebugFlagSet()) {
 			this.f.read(this.buffer, 0, 16);
 			this.offset += 16;
 
 			sf.setIndexSignature(new String(this.buffer, 0, 16));
 		}
 
 		// Get all tile offsets (TileNr, Data, Water Block)
 		for (long i = 0; i < numBlocks; i++) {
 			long val = getNextLong5();
			System.out.println(i + ": Offset: " + this.offset + " long5: " + val);
 			sf.addIndexEntry(val);
 		}
 
 		System.out.println("There are " + sf.getAmountOfTilesInIndex() + " tiles in the index.");
 
 		Tile t = null;
 		int tilesProcessed = 0;
 		for (int i = 0; i < sf.getAmountOfTilesInIndex(); i++) {
 			++tilesProcessed;
 			System.out.print("Get next tile (" + (i + 1) + " / " + numBlocks + ") @ "
 					+ this.offset
					+ "(" + (sf.getTileOffset(i) + indexOffset) + ")...");
 			if (sf.isEmptyTile(i)) {
 				sf.addTile(null);
				System.out.println("skipped.");
 				continue;
 			}
 			t = getNextTile(sf, zoomInterval);
 			sf.addTile(t);
 			System.out.println("done.");
 		}
 
 		System.out.println("Processed " + tilesProcessed + " tiles.");
 
 		return sf;
 	}
 
 	private Tile getNextTile(SubFile sf, byte zoomInterval) throws IOException {
 
 		Tile t;
 		t = new Tile(this.mapFile, sf);
 
 		// H E A D E R
 
 		// Tile signature (32B, optional)
 		// ###TileStart
 		if (this.mapFile.isDebugFlagSet()) {
 			this.f.seek(this.offset);
 			this.f.read(this.buffer, 0, 32);
 			this.offset += 32;
 			t.setTileSignature(new String(this.buffer, 0, 32));
 		}
 
 		// Zoom table (variable)
 		for (int row = this.mapFile.getMinimalZoomLevel()[zoomInterval]; row <= this.mapFile
 				.getMaximalZoomLevel()[zoomInterval]; row++) {
 			t.addZoomTableRow(row, getNextDword(), getNextDword());
 		}
 
 		// First way offset (VBE-U)
 		t.setFirstWayOffset(getNextVBEUInt());
 
 		// FINISHED TILE //
 
 		// P O I s
 		// TODO get base zoom level
 		// TODO save data
 		// System.out.println("This tile has " + t.getCumulatedNumberOfPoisOnZoomlevel(this.mapFile
 		// .getMaximalZoomLevel()[zoomInterval]) + " POIs");
 		for (int poi = 0; poi < t.getCumulatedNumberOfPoisOnZoomlevel(this.mapFile
 				.getMaximalZoomLevel()[zoomInterval]); poi++) {
 			// TODO add POIs to tile
 			getNextPOI();
 		}
 
 		// W A Y S
 		// TODO save data
 		// System.out.println("This tile has " + t.getCumulatedNumberOfWaysOnZoomLevel(this.mapFile
 		// .getMaximalZoomLevel()[zoomInterval]) + " ways");
 		for (int way = 0; way < t.getCumulatedNumberOfWaysOnZoomLevel(this.mapFile
 				.getMaximalZoomLevel()[zoomInterval]); way++) {
 			getNextWay();
 		}
 
 		return t;
 
 	}
 
 	private POI getNextPOI() throws IOException {
 		POI p = new POI();
 
 		// POI signature (32B, optional)
 		if (this.mapFile.isDebugFlagSet()) {
 			this.f.seek(this.offset);
 			this.f.read(this.buffer, 0, 32);
 			this.offset += 32;
 			p.setPoiSignature(new String(this.buffer, 0, 32));
 		}
 
 		// Position (2 * VBE-S)
 		p.setPosition(getNextVBESInt(), getNextVBESInt());
 
 		// Special byte (1B)
 		p.setSpecialByte(getNextByte());
 
 		// POI tags (variable)
 		for (byte j = 0; j < p.getAmountOfTags(); j++) {
 			p.addTagID(getNextVBEUInt());
 		}
 
 		// Flags (1B)
 		p.setFlags(getNextByte());
 
 		// POI name (variable, optional)
 		if (p.isPOINameFlagSet()) {
 			p.setName(getNextString());
 		}
 
 		// POI elevation (VBE-S, optional)
 		if (p.isElevationFlagSet()) {
 			p.setElevation(getNextVBESInt());
 		}
 
 		// House number (String, optional)
 		if (p.isHouseNumberFlagSet()) {
 			p.setHouseNumber(getNextString());
 		}
 
 		return p;
 	}
 
 	private Way getNextWay() throws IOException {
 		Way w = new Way();
 
 		// Way signature (32B, optional)
 		if (this.mapFile.isDebugFlagSet()) {
 			this.f.seek(this.offset);
 			this.f.read(this.buffer, 0, 32);
 			this.offset += 32;
 			w.setWaySignature(new String(this.buffer, 0, 32));
 		}
 
 		// Way size (VBE-U)
 		w.setWaySize(getNextVBEUInt());
 
 		// Sub tile bitmap (2B)
 		w.setSubTileBitmap(getNextByte(), getNextByte());
 
 		// Special byte 1 (1B)
 		w.setSpecialByte1(getNextByte());
 
 		// Special byte 2 (1B)
 		w.setSpecialByte2(getNextByte());
 
 		// Way type bitmap (1B)
 		w.setWayTypeBitmap(getNextByte());
 
 		// Tag ID (n * VBE-U)
 		for (byte tag = 0; tag < w.getAmountOfTags(); tag++) {
 			w.addTagID(getNextVBEUInt());
 		}
 
 		// Way node amount (VBE-U)
 		w.setWayNodeAmount(getNextVBEUInt());
 
 		// First way node (2*VBE-S)
 		w.setFirstWayNodeLatDiff(getNextVBESInt());
 		w.setFirstWayNodeLonDiff(getNextVBESInt());
 
 		// Way nodes (n*2*VBE-S)
 		for (int i = 0; i < w.getWayNodeAmount() - 1; i++) {
 			w.addWayNodesLatDiff(getNextVBESInt());
 			w.addWayNodesLonDiff(getNextVBESInt());
 		}
 
 		// Flags (1B)
 		w.setFlags(getNextByte());
 
 		// Name (String, optional)
 		if (w.isWayFlagSet()) {
 			w.setName(getNextString());
 		}
 
 		// Reference (String, optional)
 		if (w.isReferenceFlagSet()) {
 			w.setReference(getNextString());
 		}
 
 		// Label position (2*VBE-S, optional)
 		if (w.isLabelPositionFlagSet()) {
 			w.setLabelPositionLatDiff(getNextVBESInt());
 			w.setLabelPositionLonDiff(getNextVBESInt());
 		}
 
 		// Multipolygon (variable, optional)
 		// TODO save values
 		if (w.isMultipolygonFlagSet()) {
 			int amountOfInnerWays = getNextVBEUInt();
 
 			// Remaining inner way nodes
 			for (int i = 0; i < amountOfInnerWays; ++i) {
 				int amountOfInnerWayNodes = getNextVBEUInt();
 
 				for (int j = 0; j < amountOfInnerWayNodes; j++) {
 					getNextVBESInt();
 					getNextVBESInt();
 				}
 
 			}
 		}
 
 		return w;
 	}
 
 	static String getHex(byte[] raw) {
 		if (raw == null) {
 			return null;
 		}
 		final StringBuilder hex = new StringBuilder(2 * raw.length + 2);
 		hex.append("0x");
 		for (final byte b : raw) {
 			hex.append(MapFormatReader.HEXES.charAt((b & 0xF0) >> 4))
 					.append(MapFormatReader.HEXES.charAt((b & 0x0F)));
 		}
 		return hex.toString();
 	}
 
 	static String getHex(int raw) {
 		return MapFormatReader.getHex(new byte[] { (byte) (raw >>> 24), (byte) (raw >>> 16),
 				(byte) (raw >>> 8), (byte) (raw & 0xff) });
 	}
 
 	static String getHex(long raw) {
 		return MapFormatReader.getHex(new byte[] { (byte) (raw >>> 56), (byte) (raw >>> 48),
 				(byte) (raw >>> 40), (byte) (raw >>> 32), (byte) (raw >>> 24), (byte) (raw >>> 16),
 				(byte) (raw >>> 8), (byte) (raw & 0xff) });
 	}
 
 	static String getHex(byte raw) {
 		return MapFormatReader.getHex(new byte[] { raw });
 	}
 
 	private static int byteArrayToInt(byte[] bytes) {
 		return (bytes[0] << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8)
 				| (bytes[3] & 0xff);
 	}
 
 	private static long byteArrayToLong(byte[] bytes) {
 		ByteBuffer bb = ByteBuffer.wrap(bytes);
 		return bb.getLong();
 	}
 
 	private int getNextInt() throws IOException {
 		this.f.seek(this.offset);
 		this.f.read(this.buffer, 0, 4);
 		this.offset += 4;
 		return MapFormatReader.byteArrayToInt(this.buffer);
 	}
 
 	private byte getNextByte() throws IOException {
 		this.f.seek(this.offset);
 		this.f.read(this.buffer, 0, 1);
 		this.offset += 1;
 
 		return this.buffer[0];
 	}
 
 	private String getNextString() throws IOException {
 		int strlen = getNextVBEUInt();
 		this.f.seek(this.offset);
 		this.f.read(this.buffer, 0, strlen);
 		this.buffer[strlen] = '\0';
 		this.offset += strlen;
 
 		return new String(this.buffer).substring(0, strlen);
 	}
 
 	private int getNextDword() throws IOException {
 		this.f.seek(this.offset);
 		this.f.read(this.buffer, 0, 2);
 		this.offset += 2;
 
 		return (this.buffer[0] << 8) + (this.buffer[1] & 0xff);
 	}
 
 	private long getNextLong() throws IOException {
 		this.f.seek(this.offset);
 		this.f.read(this.buffer, 0, 8);
 		this.offset += 8;
 
 		return MapFormatReader.byteArrayToLong(buffer);
 	}
 
 	private long getNextLong5() throws IOException {
 
 		this.f.seek(this.offset);
 		this.f.read(this.buffer, 0, 5);
 
 		long ret = (this.buffer[0] & 0xffL) << 32 | (this.buffer[1] & 0xffL) << 24
 				| (this.buffer[2] & 0xffL) << 16 | (this.buffer[3] & 0xffL) << 8
 				| (this.buffer[4] & 0xffL);
 		ret = ret & 0x7FFFFFFFFFL;
 
 		this.offset += 5;
 
 		return ret;
 
 	}
 
 	private int getNextVBEUInt() throws IOException {
 		byte shift = 0;
 		int ret = 0;
 		byte b;
 
 		// Bytes with continuation bit (low order bytes)
 		while (((b = getNextByte()) & 0x80) != 0) {
 			ret |= (b & 0x7f) << shift;
 			shift += 7;
 		}
 
 		// High order byte (last byte)
 		ret |= (b & 0x7f) << shift;
 
 		return ret;
 	}
 
 	private int getNextVBESInt() throws IOException {
 		// TODO test
 		byte shift = 0;
 		int ret = 0;
 		byte b;
 
 		// Bytes with continuation bit (low order bytes)
 		while (((b = getNextByte()) & 0x80) != 0) {
 			ret |= (b & 0x7f) << shift;
 			shift += 7;
 		}
 
 		// High order byte (last byte)
 		ret |= (b & 0x7f) << shift;
 
 		return (ret & 0x80000000) == 0 ? ret : -ret;
 	}
 }
