 /*************************************************************************
  * Copyright (C) 2012 Philippe Leipold
  *
  * This file is part of CreativePlus.
  *
  * CreativePlus is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CreativePlus is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with CreativePlus. If not, see <http://www.gnu.org/licenses/>.
  *
  **************************************************************************/
 
 package de.Lathanael.CP.Protect;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import de.Lathanael.BinaryFileDB.API.DBAccess;
 import de.Lathanael.BinaryFileDB.API.RecordReader;
 import de.Lathanael.BinaryFileDB.API.RecordWriter;
 import de.Lathanael.BinaryFileDB.API.SubDirFileFilter;
 import de.Lathanael.BinaryFileDB.API.SubDirFileFilter.Type;
 import de.Lathanael.BinaryFileDB.Exception.CacheSizeException;
 import de.Lathanael.BinaryFileDB.Exception.QueueException;
 import de.Lathanael.BinaryFileDB.Exception.RecordsFileException;
 
 /**
  * @author Lathanael (aka Philippe Leipold)
  *
  */
 public class ChunkFiles {
 
 	private static final String filePrefix = "chunks.";
 	private static SubDirFileFilter filter = new SubDirFileFilter();
 	private static String path;
 	private static String extension;
 	private static HashMap<String, File> existingFiles;
 	private static TreeMap<String, DBAccess> loadedDataBases = new TreeMap<String, DBAccess>();
 
 	public static void initFiles(String path, String extension) {
 		ChunkFiles.path = path;
 		ChunkFiles.extension = extension;
 		List<File> files= filter.getFiles(new File(path), filter.new PatternFilter(Type.FILE, extension), true);
 		existingFiles = new HashMap<String, File>((int) (files.size()/0.75) + 1);
 		String name;
 		for (File file : files) {
 			int lastDot = file.getName().lastIndexOf('.');
 			if (0 < lastDot && lastDot <= file.length()) {
 				name = file.getName().substring(0, lastDot);
 				existingFiles.put(name, file);
 			}
 		}
 	}
 
 	public static void closeFiles() {
 		for (Map.Entry<String, DBAccess> entry : loadedDataBases.entrySet())
 			try {
 				entry.getValue().closeDB(false);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (RecordsFileException e) {
 				e.printStackTrace();
 			}
 	}
 
 	public static void add(int chunkX, int chunkZ, ChunkBlockLocation cbl) {
 		int fileExX = chunkX >> 3;
 		int fileExZ = chunkZ >> 3;
 		String newFileName = filePrefix + String.valueOf(fileExX) + "." + String.valueOf(fileExZ);
 		String newChunkName = "chunk." + String.valueOf(chunkX) + "." + String.valueOf(chunkZ);
 		DBAccess database = null;
 		try {
 			if (loadedDataBases.containsKey(newFileName)) {
 				database = loadedDataBases.get(newFileName);
 				if (database != null) {
 					RecordReader rr = database.getRecord(newChunkName);
 					if (rr != null) {
 						CBLSet blocks = (CBLSet) rr.readObject();
 						blocks.getBlocks().add(cbl);
 						RecordWriter rw = new RecordWriter(newChunkName);
 						rw.writeObject(blocks);
 						database.writeRecord(rw);
 						return;
 					} else {
 						final HashSet<ChunkBlockLocation> blocks = new HashSet<ChunkBlockLocation>();
 						blocks.add(cbl);
 						CBLSet blockSet = new CBLSet(blocks);
 						RecordWriter rw = new RecordWriter(newChunkName);
 						rw.writeObject(blockSet);
 						database.writeRecord(rw);
 						return;
 					}
 				}
 			}
 			if (existingFiles.keySet().contains(newFileName))
 				database = new DBAccess(existingFiles.get(newFileName).getPath(), "rw", true, 5);
 			else
 				database = new DBAccess(path + File.separator + newFileName + extension, 10, true, 5);
 			if (database != null) {
 				RecordReader rr = database.getRecord(newChunkName);
 				if (rr != null) {
 					CBLSet blocks = (CBLSet) rr.readObject();
 					blocks.getBlocks().add(cbl);
 					RecordWriter rw = new RecordWriter(newChunkName);
 					rw.writeObject(blocks);
 					database.writeRecord(rw);
 				} else {
 					final HashSet<ChunkBlockLocation> blocks = new HashSet<ChunkBlockLocation>();
 					blocks.add(cbl);
 					CBLSet blockSet = new CBLSet(blocks);
 					RecordWriter rw = new RecordWriter(newChunkName);
 					rw.writeObject(blockSet);
 					database.writeRecord(rw);
 					return;
 				}
 				loadedDataBases.put(newFileName, database);
 				return;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		} catch (RecordsFileException e) {
 			e.printStackTrace();
 			return;
 		} catch (CacheSizeException e) {
 			e.printStackTrace();
 			return;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (QueueException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static boolean isProtected(int chunkX, int chunkZ, ChunkBlockLocation cbl) {
 		if (cbl == null)
 			return false;
 		int fileExX = chunkX >> 3;
 		int fileExZ = chunkZ >> 3;
 		String newFileName = filePrefix + String.valueOf(fileExX) + "." + String.valueOf(fileExZ);
 		String newChunkName = "chunk." + String.valueOf(chunkX) + "." + String.valueOf(chunkZ);
 		DBAccess database = null;
 		try {
 			if (loadedDataBases.containsKey(newFileName)) {
 				database = loadedDataBases.get(newFileName);
 				if (database != null) {
 					RecordReader rr;
 					rr = database.getRecord(newChunkName);
 					if (rr != null) {
 						CBLSet blocks = (CBLSet) rr.readObject();
 						if (blocks == null)
 							return false;
 						return blocks.getBlocks().contains(cbl);
 					} else
 						return false;
 				}
 			} else if (existingFiles.keySet().contains(newFileName)) {
 				database = new DBAccess(existingFiles.get(newFileName).getPath(), "rw", true, 5);
 				if (database != null) {
 					RecordReader rr;
 					rr = database.getRecord(newChunkName);
 					if (rr != null) {
 						CBLSet blocks = (CBLSet) rr.readObject();
 						if (blocks == null)
 							return false;
 						return blocks.getBlocks().contains(cbl);
 					} else
 						return false;
 				}
 			} else
 				return false;
 		} catch (RecordsFileException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (CacheSizeException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 }
