 /*
     netcdf-java-wdb
 
     Copyright (C) 2011 met.no
 
     Contact information:
     Norwegian Meteorological Institute
     Box 43 Blindern
     0313 OSLO
     NORWAY
     E-mail: post@met.no
 
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
     MA  02110-1301, USA
  */
 
 package no.met.wdb.netcdf;
 
 import java.io.IOException;
 import java.nio.channels.WritableByteChannel;
 import java.sql.SQLException;
 import java.util.Vector;
 
 import no.met.wdb.Grid;
 import no.met.wdb.GridData;
 import no.met.wdb.InvalidWdbConfigurationFileException;
 import no.met.wdb.ReadQuery;
 import no.met.wdb.WdbConfiguration;
 import no.met.wdb.WdbConnection;
 import no.met.wdb.store.IndexCreationException;
 import no.met.wdb.store.WdbIndex;
 
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.ma2.Section;
 import ucar.ma2.StructureDataIterator;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.ParsedSectionSpec;
 import ucar.nc2.Structure;
 import ucar.nc2.Variable;
 import ucar.nc2.iosp.IOServiceProvider;
 import ucar.nc2.util.CancelTask;
 
 /**
  * IOServiceProvider for accessing a wdb database
  */
 public class WdbIOServiceProvider implements IOServiceProvider {
 
 	private WdbConnection connection = null;
 	private NetcdfIndexBuilder index = null; 
 	
 	@Override
 	public boolean isValidFile(ucar.unidata.io.RandomAccessFile raf)
 			throws IOException {
 		raf.seek(0);
 		try {
 			new WdbConfiguration(raf);
 		}
 		catch ( InvalidWdbConfigurationFileException e ) {
 			return false;
 		}
 		return true;
 	}
 
 	
 	@Override
 	public void open(ucar.unidata.io.RandomAccessFile raf, NetcdfFile ncfile,
 			CancelTask cancelTask) throws IOException {
 		try {
 			connection = new WdbConnection(new WdbConfiguration(raf));
 
 			Vector<String> dataProvider = new Vector<String>();
			dataProvider.add("pgen_probability");//"met.no eceps modification");//"nordic");
 			//Vector<String> parameters = new Vector<String>();
 			//parameters.add("sea water temperature");
 			Vector<String> parameters = null;
 			ReadQuery readQuery = new ReadQuery(dataProvider, null, null, null, parameters, null, null);
 			
 			if ( cancelTask != null && cancelTask.isCancel() )
 				return;
 
 			Iterable<GridData> gridData = connection.readGid(readQuery);
 			
 			index = new NetcdfIndexBuilder(gridData, new GlobalWdbConfiguration());
 
 			if ( cancelTask != null && cancelTask.isCancel() )
 				return;
 
 			index.populate(ncfile);
 			
 			ncfile.finish();
 		}
 		catch ( InvalidWdbConfigurationFileException e ) {
 			throw new IOException(e);
 		}
 		catch ( IndexCreationException e ) {
 			throw new IOException(e);
 		}
 		catch ( ClassNotFoundException e ) {
 			throw new IOException(e);
 		} 
 		catch ( SQLException e ) {
 			throw new IOException(e);
 		} 
 	}
 
 	@Override
 	public ucar.ma2.Array readData(Variable v2, Section section)
 			throws java.io.IOException, InvalidRangeException {
 
 		System.out.println("public ucar.ma2.Array readData(Variable(" + v2.getName() + "), Section(" + section.toString() + "))");
 		
 		ucar.ma2.Array ret;
 
 		if ( index.isDatabaseField(v2.getName()) ) {
 			ret = Array.factory(DataType.FLOAT, section.getShape());
 			long[] gids = index.getGridIdentifiers(v2, section);
 
 			try {
 				int idx = 0;
 				for ( long g : gids ) {
 					int rank = section.getRank();
 					Range yRange = section.getRange(rank -2);
 					Range xRange = section.getRange(rank -1);
 
 					if ( g != WdbIndex.UNDEFINED_GID ) {
 						Grid grid = connection.getGrid(g);
 						float[] data = grid.getGrid();
 						for ( int y = yRange.first(); y <= yRange.last(); y += yRange.stride() )
 							for ( int x = xRange.first(); x <= xRange.last(); x += xRange.stride() ) {
 								int gridIdx = (y * grid.getNumberX()) + x;
 								ret.setFloat(idx ++, data[gridIdx]);
 							}
 					}
 					else
 						for ( int y = yRange.first(); y <= yRange.last(); y += yRange.stride() )
 							for ( int x = xRange.first(); x <= xRange.last(); x += xRange.stride() )
 								ret.setFloat(idx ++, Float.NaN);
 				}
 			}
 			catch (SQLException e) {
 				throw new IOException(e);
 			}
 		}
 		else
 			ret = index.getMetadata(v2, section);
 		
 		if ( ret == null )
 			throw new IOException("Internal error: Unable to read data: " + v2.getName());
 		
 		return ret;
 	}
 
 	
 	@Override
 	public long readToByteChannel(Variable v2, Section section,
 			WritableByteChannel channel) throws java.io.IOException,
 			InvalidRangeException {
 
 		System.out.println("public long readToByteChannel(Variable v2, Section section,	WritableByteChannel channel)");
 		
 		throw new InvalidRangeException();
 	}
 
 
 	@Override
 	public Array readSection(ParsedSectionSpec cer) throws IOException,
 			InvalidRangeException {
 		
 		System.out.println("public Array readSection(ParsedSectionSpec cer)");
 		
 		return null;
 	}
 
 	@Override
 	public StructureDataIterator getStructureIterator(Structure s,
 			int bufferSize) throws java.io.IOException {
 		
 		System.out.println("public StructureDataIterator getStructureIterator(Structure s, int bufferSize)");
 		
 		return null;
 	}
 
 	@Override
 	public void close() throws IOException {
 		if ( connection != null )
 			connection.close();
 	}
 
 	@Override
 	public boolean syncExtend() throws IOException {
 		return false;
 	}
 
 	@Override
 	public boolean sync() throws IOException {
 		return false;
 	}
 
 	@Override
 	public Object sendIospMessage(Object message) {
 		return null;
 	}
 
 	@Override
 	public String toStringDebug(Object o) {
 		return "wdb iosp";
 	}
 
 	@Override
 	public String getDetailInfo() {
 		return "wdb iosp";
 	}
 
 	@Override
 	public String getFileTypeId() {
 		return "wbd";
 	}
 
 	@Override
 	public String getFileTypeVersion() {
 		return "1.0.0";
 	}
 
 	@Override
 	public String getFileTypeDescription() {
 		return "wdb connection";
 	}
 }
