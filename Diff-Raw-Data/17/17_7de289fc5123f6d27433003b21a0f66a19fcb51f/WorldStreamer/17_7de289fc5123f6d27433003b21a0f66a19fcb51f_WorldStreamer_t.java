 package com.hexcore.cas.model;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.utilities.Log;
 
 public class WorldStreamer
 {
 	private boolean reset = false;
 	private boolean started = false;
 	private int numGenerations = 0;
 	private String cawFilename = null;
 	private String tmpDir = null;
 
 	private static final String TAG = "WorldStreamer";
 	
 	public WorldStreamer()
 	{
 	}
 	
 	public Grid getGeneration(int genNum)
 	{
 		File folder = new File(tmpDir);
 		File currFile = null;
 		File[] listOfFiles = folder.listFiles();
 		
 		InputStream in = null;
 		String currFilename = null;
 		
 		try
 		{
 			boolean configFound = false;
 			boolean genFound = false;
 			char type = 'N';
 			Grid gen = null;
 			int properties = 0;
 			int x = -1;
 			int y = -1;
 			
 			//Find the config file.
 			for(int i = 0; i < listOfFiles.length; i++)
 			{
 				if(listOfFiles[i].isFile())
 				{
 					currFilename = listOfFiles[i].getName();
 					currFile = new File(tmpDir + "/" + currFilename);
 					in = new BufferedInputStream(new FileInputStream(currFile));
 					
 					String ext = currFilename.substring(currFilename.lastIndexOf(".") + 1);
 					String fileData = "";
 					
 					byte[] data = new byte[1024];
 					int len = 1024;
 					while((len = in.read(data)) > 0)
 					{
 						fileData += new String(data);
 					}
 					
 					StringTokenizer token = new StringTokenizer(fileData);
 					
 					if(ext.compareTo("cac") == 0)
 					{
 						configFound = true;
 						
 						x = Integer.parseInt(token.nextToken());
 						y = Integer.parseInt(token.nextToken());
 						type = token.nextToken().charAt(0);
 						properties = Integer.parseInt(token.nextToken());
 
 						Vector2i gridSize = new Vector2i(x, y);
 						Cell cell = new Cell(properties);
 						switch(type)
 						{
 							case 'r':
 							case 'R':
 								gen = new RectangleGrid(gridSize, cell);
 								break;
 							case 'h':
 							case 'H':
 								gen = new HexagonGrid(gridSize, cell);
 								break;
 							case 't':
 							case 'T':
 								gen = new TriangleGrid(gridSize, cell);
 								break;
 							default:
 								Log.error(TAG, "Unable to create a grid with no type.");
 								return null;
 						}
 						
 						break;
 					}
 					else
 						continue;
 				}
 			}
 			
 			//Find the generation files.
 			for(int i = 0; i < listOfFiles.length; i++)
 			{
 				if(listOfFiles[i].isFile())
 				{
 					currFilename = listOfFiles[i].getName();
 					currFile = new File(tmpDir + "/" + currFilename);
 					in = new BufferedInputStream(new FileInputStream(currFile));
 					
 					String fileData = "";
 					
 					byte[] data = new byte[1024];
 					int len = 1024;
 					while((len = in.read(data)) > 0)
 					{
 						fileData += new String(data);
 					}
 					
 					StringTokenizer token = new StringTokenizer(fileData);
 					
 					if(!configFound)
 					{
 						Log.error(TAG, "Configuration file not found.");
 						return null;
 					}
 					
 					int index = currFilename.lastIndexOf('/');
 					if(index == -1)
 						index = 0;
 					
 					if(currFilename.substring(index).compareTo(genNum + ".cag") == 0)
 					{
 						genFound = true;
 
 						for(int rows = 0; rows < y; rows++)
 						{
 							for(int cols = 0; cols < x; cols++)
 							{
 								double[] vals = new double[properties];
 
 								for(int j = 0; j < properties; j++)
 									vals[j] = Double.parseDouble(token.nextToken());
 								
 								gen.setCell(cols, rows, vals);
 							}
 						}
 					}
 				}
 			}
 
 			if(!genFound)
 			{
 				Log.error(TAG, "Generation file " + genNum + " not found.");
 				return null;
 			}
 			
 			in.close();
 			in = null;
 			
 			return gen;
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Error retrieving all generations - " + ex.getMessage());
 			ex.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<Grid> getGenerations(World w)
 	{
 		List<Grid> generations = Collections.synchronizedList(new ArrayList<Grid>());
 		File folder = new File(tmpDir);
 		File currFile = null;
 		File[] listOfFiles = folder.listFiles();
 		int arrSize = listOfFiles.length - 3;
 		Grid[] gens = new Grid[arrSize];
 		InputStream in = null;
 		String currFilename = null;
 		
 		try
 		{
 			boolean configFound = false;
 			char type = 'N';
 			Grid gen = null;
 			int properties = 0;
 			int x = -1;
 			int y = -1;
 			int genNum = 0;
 			
 			//Find the config file.
 			for(int i = 0; i < listOfFiles.length; i++)
 			{
 				if(listOfFiles[i].isFile())
 				{
 					currFilename = listOfFiles[i].getName();
 					currFile = new File(tmpDir + "/" + currFilename);
 					in = new BufferedInputStream(new FileInputStream(currFile));
 					
 					String ext = currFilename.substring(currFilename.lastIndexOf(".") + 1);
 					String fileData = "";
 					
 					byte[] data = new byte[1024];
 					int len = 1024;
 					while((len = in.read(data)) > 0)
 					{
 						fileData += new String(data);
 					}
 					
 					StringTokenizer token = new StringTokenizer(fileData);
 					
 					if(ext.compareTo("cac") == 0)
 					{
 						configFound = true;
 						
 						x = Integer.parseInt(token.nextToken());
 						y = Integer.parseInt(token.nextToken());
 						type = token.nextToken().charAt(0);
 						properties = Integer.parseInt(token.nextToken());
 
 						Vector2i gridSize = new Vector2i(x, y);
 						Cell cell = new Cell(properties);
 						switch(type)
 						{
 							case 'r':
 							case 'R':
 								gen = new RectangleGrid(gridSize, cell);
 								break;
 							case 'h':
 							case 'H':
 								gen = new HexagonGrid(gridSize, cell);
 								break;
 							case 't':
 							case 'T':
 								gen = new TriangleGrid(gridSize, cell);
 								break;
 							default:
 								Log.error(TAG, "Unable to create a grid with no type.");
 								return null;
 						}
 						
 						break;
 					}
 					else
 						continue;
 				}
 			}
 			
 			//Find the generation files.
 			for(int i = 0; i < listOfFiles.length; i++)
 			{
 				if(listOfFiles[i].isFile())
 				{
 					currFilename = listOfFiles[i].getName();
 					currFile = new File(tmpDir + "/" + currFilename);
 					in = new BufferedInputStream(new FileInputStream(currFile));
 					
 					String ext = currFilename.substring(currFilename.lastIndexOf(".") + 1);
 					String fileData = "";
 					
 					byte[] data = new byte[1024];
 					int len = 1024;
 					while((len = in.read(data)) > 0)
 					{
 						fileData += new String(data);
 					}
 					
 					StringTokenizer token = new StringTokenizer(fileData);
 					
 					if(ext.compareTo("cag") == 0)
 					{
 						if(!configFound)
 						{
 							Log.error(TAG, "Configuration file not found.");
 							return null;
 						}
 						
 						int index = currFilename.lastIndexOf('/');
 						if(index == -1)
 							index = 0;
 						int fileGenIndex = currFilename.indexOf(".cag");
 						int fileGenNum = 0;
 						if(index != 0)
 							fileGenNum = Integer.parseInt(currFilename.substring(index + 1, fileGenIndex));
 						else
 							fileGenNum = Integer.parseInt(currFilename.substring(index, fileGenIndex));
 						
 						Log.debug(TAG, "File name : " + currFilename + "; File generation number : " + fileGenNum);
 						
 						for(int rows = 0; rows < y; rows++)
 						{
 							for(int cols = 0; cols < x; cols++)
 							{
 								double[] vals = new double[properties];
 
 								for(int j = 0; j < properties; j++)
 									vals[j] = Double.parseDouble(token.nextToken());
 								
 								gen.setCell(cols, rows, vals);
 							}
 						}
 						
 						gens[fileGenNum] = gen.clone();
 						genNum++;
 					}
 					else
 						continue;
 				}
 			}
 
 			in.close();
 			in = null;
 			
 			for(int i = 0; i < arrSize; i++)
 				generations.add(gens[i]);
 			
 			return generations;
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Error retrieving all generations - " + ex.getMessage());
 			ex.printStackTrace();
 			return null;
 		}
 	}
 	
 	public Grid getLastGeneration()
 	{
 		return getGeneration(numGenerations - 1);
 	}
 	
 	public boolean hasStarted()
 	{
 		return started;
 	}
 	
 	public void reset(World w)
 	{
 		if(started)
 		{
 			reset = true;
 			start(w);
 		}
 		else
 		{
 			reset = false;
 			start(w);
 		}
 	}
 	
 	public void start(World w)
 	{
 		if(!reset)
 		{
 			File caw = null;
 			if(w.getFilename() == null)
 				w.setFileName("worlds/newWorld.caw");
 			
 			int cawIndex = w.getFilename().indexOf(".caw");
 			
 			String name = (cawIndex != -1 ? w.getFilename() : w.getFilename() + ".caw");
 			
 			if(cawFilename == null)
 				cawFilename = name;
 			
 			caw = new File(name);
 			int worldNum = 1;
 			
 			while(caw.exists())
 			{
 				int underIndex = name.indexOf("_");
 				if(underIndex != -1)
 					name = name.substring(0, name.indexOf("_")) + "_" + worldNum + ".caw";
 				else
 					name = name.substring(0, name.indexOf(".caw")) + "_" + worldNum + ".caw";
 				
 				caw = new File(name);
 				worldNum++;
 			}
 			
 			cawFilename = name;
 			
 			tmpDir = "worlds/[tmp]" + cawFilename.substring(cawFilename.lastIndexOf("/") + 1, cawFilename.lastIndexOf("."));
 			boolean res = (new File(tmpDir).mkdirs()); 
 			
 			Log.debug(TAG, "cawFilename : " + cawFilename);
 			Log.debug(TAG, "tmpDir : " + tmpDir);
 		}
 		
 		numGenerations = 0;
 		
 		writeInitialValues(w);
 		
 		started = true;
 	}
 	
 	public void stop()
 	{
 		File folder = new File(tmpDir);
 		File caw = new File(cawFilename);
 		File[] listOfFiles = folder.listFiles();

		folder.deleteOnExit();
 		
 		try
 		{
 			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(caw)));
 		
 			for(int i = 0; i < listOfFiles.length; i++)
 			{
 				if(listOfFiles[i].isFile())
 				{
 					String currFilename = listOfFiles[i].getName();
 					File currFile = new File(tmpDir + "/" + currFilename);
 					FileInputStream in = new FileInputStream(currFile);
 					ZipEntry entry = new ZipEntry(currFilename);
 					
 					out.putNextEntry(entry);
 					byte[] data = new byte[1024];
 					int len = 1024;
 					while((len = in.read(data)) > 0)
 					{
 						out.write(data, 0, len);
 					}
 					out.closeEntry();
 					
 					in.close();
 					currFile.deleteOnExit();
 				}
 			}
 			
 			out.close();
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Error in stopping - " + ex.getMessage());
 			ex.printStackTrace();
 		}
 	}
 	
 	public void streamGeneration(Grid gen)
 	{
 		OutputStream out = null;
 		File genFile = null;
 		String data = "";
 		
 		try
 		{
 			int genNum = numGenerations;
 			
 			genFile = new File(tmpDir + "/" + genNum + ".cag");
 			if(!genFile.createNewFile())
 			{
 				System.out.println("Could not create tmp generation " + genNum + " file.");
 				System.exit(1);
 			}
 			out = new BufferedOutputStream(new FileOutputStream(genFile));
 			
 			for(int rows = 0; rows < gen.getHeight(); rows++)
 			{
 				for(int cols = 0; cols < gen.getWidth(); cols++)
 				{
 					for(int index = 0; index < gen.getNumProperties() - 1; index++)
 						data += gen.getCell(cols, rows).getValue(index) + " ";
 					data += gen.getCell(cols, rows).getValue(gen.getNumProperties() - 1) + "\n";
 				}
 				data += "\n";
 			}
 			
 			out.write(data.getBytes());
 			out.close();
 			
 			numGenerations++;
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Error streaming generation to the world - " + ex.getMessage());
 			ex.printStackTrace();
 		}
 	}
 	
 	public void writeInitialValues(World w)
 	{
 		OutputStream out = null;
 		File currFile = null;
 		String data = "";
 		
 		w.setKeepHistory(1);
 		
 		try
 		{
 			Grid genZero = w.getInitialGeneration();
 			char type = genZero.getTypeSymbol();
 			int height = genZero.getHeight();
 			int props = genZero.getNumProperties();
 			int width = genZero.getWidth();
 			
 			currFile = new File(tmpDir + "/config.cac");
 			out = new BufferedOutputStream(new FileOutputStream(currFile));
 			data = width + " " + height + "\n";
 			data += type + "\n";
 			data += props + "\n";
 			out.write(data.getBytes());
 			out.close();
 			
 			currFile = null;
 			data = "";
 			out = null;
 			
 			currFile = new File(tmpDir + "/rules.car");
 			out = new BufferedOutputStream(new FileOutputStream(currFile));
 			out.write(w.getRuleCode().getBytes());
 			out.close();
 			
 			currFile = null;
 			data = "";
 			out = null;
 			
 			currFile = new File(tmpDir + "/colours.cacp");
 			out = new BufferedOutputStream(new FileOutputStream(currFile));
 			out.write(w.getColourCode().getBytes());
 			out.close();
 			
 			List<Grid> gens = w.getGenerations();
 			for (int i = 0; i < gens.size(); i++)
 			{
 				Grid grid = gens.get(i);
 				currFile = new File(tmpDir + "/" + i + ".cag");
 				out = new BufferedOutputStream(new FileOutputStream(currFile));
 
 				for(int rows = 0; rows < height; rows++)
 				{
 					for(int cols = 0; cols < width; cols++)
 					{
 						for(int index = 0; index < props - 1; index++)
 							data += grid.getCell(cols, rows).getValue(index) + " ";
 						data += grid.getCell(cols, rows).getValue(props - 1) + "\n";
 					}
 					data += "\n";
 				}
 				out.write(data.getBytes());
 				out.close();
 				
 				currFile = null;
 				data = "";
 				out = null;
 			}
 			
 			numGenerations = gens.size();
 			
 			w.clearHistory();
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Error writing all initial values to the world - " + ex.getMessage());
 			ex.printStackTrace();
 		}
 		
 		w.setKeepHistory(2);
 	}
 	
 	public String getCawFilename()
 	{
 		return cawFilename;
 	}
 }
