 /**
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 package org.nees.rpi.vis.loaders;
 
 import org.nees.rpi.vis.*;
 import org.nees.rpi.vis.model.*;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.commons.configuration.ConfigurationException;
 
 import java.net.URL;
 import java.util.*;
 import java.util.List;
 import java.awt.*;
 import java.io.File;
 
 import javax.vecmath.Point3f;
 
 /**
  * Reads m3dv files loading the 3DDV model from the xml
  * descriptors as well as the data from the data files.
  */
 public class M3DVLoader
 {
 	ArrayList<LoaderError> errors;
 	XMLConfiguration config = null;
 	DVModel model = null;
 
 	/** Counter used when creating names for nameless data shapes */
 	private int shapeNameCounter = 1;
 
 	public M3DVLoader()
 	{
 		errors = new ArrayList<LoaderError>();
 	}
 
 	//***************************************************************************
 	// ERROR MANAGEMENT - Start
 	public boolean isError()
 	{
 		return errors.size() > 0;
 	}
 
 	public LoaderError[] getErrors()
 	{
 		return errors.toArray(new LoaderError[errors.size()]);
 	}
 
 	void reportError(LoaderError error)
 	{
 		errors.add(error);
 	}
 
 	void reportError(String tag, String msg, String exceptionMsg)
 	{
 		reportError(new LoaderError(tag, msg, exceptionMsg));
 	}
 	// ERROR MANAGEMENT - End
 	//***************************************************************************
 
 	//***************************************************************************
 	// MODEL PROCESSING - Start
 	public DVModel loadModel(URL xmlInput)
 	{
 		try
 		{
 			config = new XMLConfiguration(xmlInput);
 			processModelInfo();
 			processDataFile(xmlInput);
 			Collection<DVGeometry> geometries = processGeometries();
 			processGroups(geometries);
 		}
 		catch (ConfigurationException e)
 		{
 			reportError("Document", "Could not load document", e.getMessage());
 		}
 
 		config = null;
 		return model;
 	}
 
     /**
      * Parses the input xml solely to get the names of the datafiles associated
      * with this model file
      * @param xmlInput
      * @return  a list of the file names for the model, returns an empty list if none
      *          found
      */
     public List<String> loadDataFileNames(URL xmlInput)
     {
         List<String> datafiles = new ArrayList<String>();
         boolean moreDataFiles = true;
 
         try
 		{
 			config = new XMLConfiguration(xmlInput);
             for (int i=0; moreDataFiles; i++)
             {
                 String filename = config.getString("data-file(" + i + ").file-name");
 
                 if (filename != null)
                     datafiles.add(filename);
                 else
                     moreDataFiles = false;
             }
 		}
 		catch (ConfigurationException e)
         {
             reportError("Document", "Could not load document", e.getMessage());
         }
 
         return datafiles;
     }
  
     private void processModelInfo()
 	{
 		model = new DVModel(config.getString("title"));
 		model.setPreparedBy(config.getString("prepared-by"));
 	}
 	// MODEL PROCESSING - End
 	//***************************************************************************
 
 	//***************************************************************************
 	// DATA FILE PROCESSING - Start
 	private void processDataFile(URL parentXMLURL)
 	{
 		boolean moreDataFiles = true;
 
 		for (int i=0; moreDataFiles; i++)
 		{
 			String filename = config.getString("data-file(" + i + ").file-name");
 			String delimiter = config.getString("data-file(" + i + ").delimiter");
 			String sSkipRows = config.getString("data-file(" + i + ").skip-rows");
 			String timeChannelName = config.getString("data-file(" + i + ").time-channel");
 
 			if (filename != null)
 			{
 				try
 				{
 					File toRead;
 					if (filename.contains("/") || filename.contains("\\"))
 						toRead = new File(filename);
 					else
 					{
 						String parentDir =
 										parentXMLURL.getPath().substring(0,parentXMLURL.getPath().lastIndexOf("/"));
 						toRead = new File(parentDir + "/" +  filename);
 					}
 
 					if (!toRead.exists())
 						throw new RuntimeException("could not find file at \"" + toRead.getAbsolutePath() + "\"");
 
 					DelimiterType delimiterType;
 
 					if (delimiter == null)
 						delimiterType = DelimiterType.TAB_DELIMITED;
 					else
 					{
 						String delimiterTemp = delimiter.trim().toLowerCase();
 						if (delimiterTemp.compareTo("tab") == 0)
 							delimiterType = DelimiterType.TAB_DELIMITED;
 						else if (delimiterTemp.compareTo("comma") == 0)
 							delimiterType = DelimiterType.COMMA_DELIMITED;
 						else
 							delimiterType = DelimiterType.OTHER;
 					}
 
 					int skipRows = 0;
 					try
 					{
 						if (sSkipRows != null)
 							skipRows = Integer.parseInt(sSkipRows);
 					}
 					catch (Exception e)
 					{
 						reportError("data-file.skip-rows", "Data file error", "cannot convert \"" + sSkipRows + "\" to a number");
 					}
 
 					DVDataFile datafile = DataFileLoader.loadFile(toRead.toURL(), skipRows, delimiterType, delimiter, null);
 					if (timeChannelName != null)
 					{
 						DVDataChannel timeChannel = datafile.getChannel(timeChannelName);
 						if (timeChannel == null)
 							reportError("data-file.time-channel", "Data file error", "cannot find the time-channel in the data-file");
 						else
 							datafile.setTimeChannel(timeChannel);
 					}
 
 					model.addDataFile(datafile);
 				}
 				catch (Exception e)
 				{
 					reportError("data-file", "Data file error", e.getMessage());
 				}
 			}
 			else
 				moreDataFiles = false;
 		}
 	}
 	// DATA FILE PROCESSING - End
 	//***************************************************************************
 	//***************************************************************************
 	// GROUP PROCESSING - Start
 	private void processGroups(Collection<DVGeometry> geometries)
 	{
 		boolean moreGroups = true;
 
 		for (int i=0; moreGroups; i++)
 		{
 			String tag = "group(" + i + ")";
 			String name = config.getString(tag + ".name");
 
 			DVAppearance appearance = processAppearance(tag);
 			BorderInfo borderInfo = processBorderInfo(tag + ".border");
 
 			if (name != null)
 			{
 				DVGroup group = new DVGroup(name, appearance);
 				group.setUIOption(DVGroup.UIOptions.SINGLE);
 				processShapes(group, tag, geometries, appearance, borderInfo);
				model.addGroup(group);
 			}
 			else
 				moreGroups = false;
 		}
 	}
 	// GROUP PROCESSING - End
 	//***************************************************************************
 
 	//***************************************************************************
 	// SHAPE PROCESSING - Start
 	private void processShapes(
 					DVGroup group,
 					String parentTag,
 					Collection<DVGeometry> geometries,
 					DVAppearance parentApp,
 					BorderInfo parentBorderInfo)
 	{
 		boolean moreShapes = true;
 		for (int i=0; moreShapes; i++)
 		{
 			String tag = parentTag + ".shape(" + i + ")";
 			Point3f location = processShapeLocation(tag);
 			Point3f endLocation = processShapeEndLocation(tag);
 			DVShape dvShape = processShapePresetShape(tag, location);
 			DVGeometry geom = processShapeGeometry(tag, geometries);
 
 			if (dvShape == null && geom != null)
 			{
 				DVAppearance appearance = processAppearance(tag, parentApp);
 
 				// Only cylinders can have end locations, the others have not been
 				// implemented yet. 4/1/2008
 				if ( !(geom instanceof DVCylinder) )
 					endLocation = null;
 
 				if (!appearance.equals(parentApp))
 					dvShape = new DVShape(geom, location, endLocation, appearance);
 				else
 					dvShape = new DVShape(geom, location, endLocation);
 			}
 
 			if (dvShape != null)
 			{
 				processShapeData(tag, dvShape);
 				String name = config.getString(tag + ".name");
 				if (name == null)
 					name = getShapeGeneratedName();
 				dvShape.setName(name);
 
 				BorderInfo borderInfo = processBorderInfo(tag + ".border", parentBorderInfo);
 				if (borderInfo != null)
 					dvShape.setBorder(borderInfo.color);
 
 				model.addShape(dvShape, group);
 			}
 			else
 				moreShapes = false;
 		}
 	}
 
 	private Point3f processShapeLocation(String tag)
 	{
 		float x = config.getFloat(tag + ".coordinates.x", -100);
 		float y = config.getFloat(tag + ".coordinates.y", -100);
 		float z = config.getFloat(tag + ".coordinates.z", -100);
 
 		return new Point3f(x, y, z);
 	}
 
 	private Point3f processShapeEndLocation(String tag)
 	{
 		if (! config.containsKey(tag + ".coordinates-end.x"))
 			return null;
 
 		float x = config.getFloat(tag + ".coordinates-end.x", -100);
 		float y = config.getFloat(tag + ".coordinates-end.y", -100);
 		float z = config.getFloat(tag + ".coordinates-end.z", -100);
 
 		return new Point3f(x, y, z);
 	}
 
 	private DVShape processShapePresetShape(String tag, Point3f location)
 	{
 		String presetShapeName = config.getString(tag + ".preset-shape");
 
 		if (presetShapeName != null)
 			return DVShapeFactory.createPresetShape(presetShapeName, location);
 
 		return null;
 	}
 
 	private DVGeometry processShapeGeometry(String tag, Collection<DVGeometry> geometries)
 	{
 		DVGeometry geom = null;
 
 		try
 		{
 			String geomId = config.getString(tag + ".geometryid");
 
 			if (geomId != null)
 			{
 				geom = getGeometry(geometries, geomId);
 				if (geom == null)
 					throw new RuntimeException("geometryid not recognized. Check the spelling or define the shape.");
 			}
 			else if(config.getKeys(tag + ".cuboid").hasNext())
 				geom = new CuboidParser().processParams(tag + ".cuboid", null);
 			else if(config.getKeys(tag + ".cube").hasNext())
 				geom = new CubeParser().processParams(tag + ".cube", null);
 			else if(config.getKeys(tag + ".cylinder").hasNext())
 				geom = new CylinderParser().processParams(tag + ".cylinder", null);
 			else if(config.getKeys(tag + ".sphere").hasNext())
 				geom = new SphereParser().processParams(tag + ".sphere", null);
 		}
 		catch(Exception e)
 		{
 			reportError(tag, "Geometry Error", e.getMessage());
 			return null;
 		}
 
 		return geom;
 	}
 
 	private void processShapeData(String shapeTag, DVShape shape)
 	{
 		boolean metaResult = processShapeDataMeta(shapeTag, shape);
 		boolean seriesResult = processShapeDataSeries(shapeTag, shape);
 		//shape has no metadata despite having series info, add it in
 		if (!metaResult && seriesResult)
 		{
 			if (shape.isPresetShapeType())
 				shape.addMetadata("Sensor Type:", shape.getPresetShapeType().toDisplayString());
 			shape.addMetadata("Location", "X:" + shape.getX() + " Y:" + shape.getY() + " Z:" + shape.getZ());
 		}
 
 		if (metaResult || seriesResult)
 		{
 			shapeNameCounter++;
 		}
 	}
 
 	private String getShapeGeneratedName()
 	{
 		return "Shape " + shapeNameCounter;
 	}
 
 	/**
 	 *
 	 * @param parentTag
 	 * @param shape
 	 * @return
 	 * 	returns true if meta tags are defined for the processed shape
 	 */
 	private boolean processShapeDataMeta(String parentTag, DVShape shape)
 	{
 		int metaCounter;
 		boolean moreMeta = true;
 		ArrayList<String> names = new ArrayList<String>();
 		ArrayList<String> values = new ArrayList<String>();
 		String metaname, metavalue, tag;
 
 		for (metaCounter=0; moreMeta; metaCounter++)
 		{
 			tag = parentTag + ".meta(" + metaCounter + ")";
 			metaname = config.getString(tag + ".name");
 			metavalue = config.getString(tag + ".value");
 			if (metaname != null)
 			{
 				names.add(metaname);
 				values.add(metavalue);
 			}
 			else
 				moreMeta = false;
 		}
 		if (names.size() > 0)
 		{
 			shape.setMetadata(
 							names.toArray(new String[names.size()]),
 							values.toArray(new String[values.size()]));
 			return true;
 		}
 		else
 			return false;
 	}
 
 	/**
 	 *
 	 * @param parentTag
 	 * @param shape
 	 * @return
 	 * 	returns true if a series is defined for the processed shape
 	 */
 	private boolean processShapeDataSeries(String parentTag, DVShape shape)
 	{
 		String seriesTag = parentTag + ".series";
 		String timeSeriesTag = parentTag + ".time-series";
 
 		String seriesX = config.getString(seriesTag + ".x");
 		String seriesY = config.getString(seriesTag + ".y");
 		String timeSeriesName = config.getString(timeSeriesTag);
 
 		if (seriesX == null && seriesY == null && timeSeriesName == null)
 			return false;
 		else if (timeSeriesName != null)
 		{
 			DVDataChannel data = model.getDataChannel(timeSeriesName);
 			if (data == null)
 				reportError(timeSeriesTag, "Malformed series", "no data found for \"" + timeSeriesName + "\"");
 			else
 			{
 				shape.setTimeSeries(data);
 				return true;
 			}
 		}
 		else if (seriesX != null && seriesY != null)
 		{
 			DVDataChannel x = model.getDataChannel(seriesX);
 			DVDataChannel y = model.getDataChannel(seriesY);
 
 			if (x == null)
 				reportError(seriesTag, "Malformed series", "no data found for \"" + seriesX + "\"");
 			else if (y == null)
 				reportError(seriesTag, "Malformed series", "no data found for \"" + seriesY + "\"");
 			else
 			{
 				shape.setSeries(x,y);
 				return true;
 			}
 		}
 		else
 			reportError(seriesTag, "Malformed series", "must define both X and Y series");
 
 		return false;
 	}
 	// SHAPE PROCESSING - End
 	//***************************************************************************
 
 	//***************************************************************************
 	// GEOMETRY PROCESSING - Start
 	abstract class GeometryParser
 	{
 		abstract DVGeometry processParams(String parentTag, String id);
 	}
 
 	class CubeParser extends GeometryParser
 	{
 		DVCube processParams(String parentTag, String id)
 		{
 			float size = config.getFloat(parentTag + ".getGroupCount", -1);
 			if (size == -1)
 				return null;
 			else
 			{
 				DVCube cube = new DVCube(size);
 				cube.setId(id);
 				return cube;
 			}
 		}
 	}
 
 	class CuboidParser extends GeometryParser
 	{
 		DVCuboid processParams(String parentTag, String id)
 		{
 			//switch y and z
 			float sizex = config.getFloat(parentTag + ".sizex", -1);
 			float sizez = config.getFloat(parentTag + ".sizey", -1);
 			float sizey = config.getFloat(parentTag + ".sizez", -1);
 
 			if (sizex == -1 || sizey == -1 || sizez == -1)
 				return null;
 			else
 			{
 				DVCuboid cuboid = new DVCuboid(sizex, sizey, sizez);
 				cuboid.setId(id);
 				return cuboid;
 			}
 		}
 	}
 
 	class CylinderParser extends GeometryParser
 	{
 		DVCylinder processParams(String parentTag, String id)
 		{
 			float radius = config.getFloat(parentTag + ".radius", -1);
 
 			if (radius == -1)
 				return null;
 			else
 			{
 				DVCylinder cylinder = new DVCylinder(radius);
 				cylinder.setId(id);
 				return cylinder;
 			}
 		}
 	}
 
 	class SphereParser extends GeometryParser
 	{
 		DVSphere processParams(String parentTag, String id)
 		{
 			float radius = config.getFloat(parentTag + ".radius", -1);
 
 			if (radius == -1)
 				return null;
 			else
 			{
 				DVSphere sphere = new DVSphere(radius);
 				sphere.setId(id);
 				return sphere;
 			}
 		}
 	}
 
 	/*
 	class InclineParams extends CuboidParams
 	{
 		GeometryFactory.Axis direction;
 
 		InclineParams processParams(String parentTag, String id)
 		{
 			InclineParams ip = new InclineParams();
 			ip.sizex = config.getFloat(parentTag + ".sizex", -1);
 			ip.sizey = config.getFloat(parentTag + ".sizey", -1);
 			ip.sizez = config.getFloat(parentTag + ".sizez", -1);
 			String direction = config.getString(parentTag + ".direction");
 
 			if (ip.sizex == -1 || ip.sizey == -1 || ip.sizez == -1)
 				return null;
 			else
 			{
 				ip.id = id;
 				if (direction != null)
 				{
 					direction = direction.trim().toUpperCase();
 					if (direction.compareTo("X+") == 0)
 						ip.direction = GeometryFactory.Axis.XPLUS;
 					else if (direction.compareTo("X-") == 0)
 						ip.direction = GeometryFactory.Axis.XMINUS;
 					else if (direction.compareTo("Z+") == 0)
 						ip.direction = GeometryFactory.Axis.ZPLUS;
 				}
 				else
 					ip.direction = GeometryFactory.Axis.XPLUS;
 				return ip;
 			}
 		}
 	}
 	*/
 
 	private Collection<DVGeometry> processGeometries()
 	{
 		ArrayList<DVGeometry> geometries = new ArrayList<DVGeometry>();
 
 		//process cuboids
 		processGeometriesHelper(geometries, "geometries.cuboid", new CuboidParser());
 		//process cubes
 		processGeometriesHelper(geometries, "geometries.cube", new CubeParser());
 		//process cubes
 		processGeometriesHelper(geometries, "geometries.cylinder", new CylinderParser());
 		//process spheres
 		processGeometriesHelper(geometries, "geometries.sphere", new SphereParser());
 
 		return geometries;
 	}
 
 	private void processGeometriesHelper(
 					ArrayList<DVGeometry> geometries,
 					String gptag,
 					GeometryParser gpType)
 	{
 		boolean moreGeometries = true;
 		String tag = "";
 
 		try
 		{
 			for (int i=0; moreGeometries; i++)
 			{
 				tag = gptag + "(" + i + ")";
 				String id = config.getString(tag + ".id");
 				if (id != null)
 				{
 					DVGeometry geom = gpType.processParams(tag, id);
 
 					if (geom != null)
 						geometries.add(geom);
 					else
 						throw new RuntimeException("Invalid geometry type or invalid properties");
 				}
 				else
 					moreGeometries = false;
 			}
 		}
 		catch (Exception e)
 		{
 			reportError(tag, "Badly formatted geometry", e.getMessage());
 		}
 	}
 
 	private DVGeometry getGeometry(Collection<DVGeometry> geometries, String id)
 	{
 		for (DVGeometry geom : geometries)
 		{
 			if (geom.getId().compareTo(id) == 0)
 				return geom;
 		}
 		return null;
 	}
 	// GEOMETRY PROCESSING - End
 	//***************************************************************************
 
 	//***************************************************************************
 	// BORDER PROCESSING - Start
 	static class BorderInfo
 	{
 		Color color;
 	}
 
 	private BorderInfo processBorderInfo(String tag)
 	{
 		return processBorderInfo(tag, null);
 	}
 
 	private BorderInfo processBorderInfo(String tag, BorderInfo parent)
 	{
 		try
 		{
 			String show = config.getString(tag + ".show");
 			if (show != null)
 			{
 				show = show.trim().toLowerCase();
 				if (show.compareTo("yes") == 0)
 				{
 					BorderInfo bi = new BorderInfo();
 					Color defaultColor;
 					if (parent == null)
 						defaultColor = AppSettings.getInstance().getDefaultShapeBorderColor();
 					else
 						defaultColor = parent.color;
 					bi.color = processColor(tag + ".color",defaultColor);
 					return bi;
 				}
 				else if (show.compareTo("no") == 0)
 					return null;
 				else
 					throw new RuntimeException("Invalid show attribute for border");
 			}
 			else if (parent != null)
 				return parent;
 		}
 		catch (Exception e)
 		{
 			reportError(tag, "Badly formatted border setting", e.getMessage());
 		}
 
 		return null;
 	}
 	// BORDER PROCESSING - End
 	//***************************************************************************
 
 	//***************************************************************************
 	// APPEARANCE PROCESSING - Start
 	private DVAppearance processAppearance(String tag)
 	{
 		AppSettings AS = AppSettings.getInstance();
 		DVAppearance dvApp = new DVAppearance(
 			AS.getDefaultShapeFillColor(),
 			AS.getDefaultShapeTransparency()
 		);
 		return processAppearance(tag,dvApp);
 	}
 
 	private DVAppearance processAppearance(String tag, DVAppearance source)
 	{
 		Color color = processColor(tag + ".color", source.getColor());
 		float transparency = processTransparency(
 						tag + ".transparency", source.getTransparency());
 		DVAppearance.AppearanceStatus starterApp =
 						processStarterAppearance(tag + ".starter-appearance", source.getAppearanceStatus());
 		if (
 					source.getColor().equals(color) &&
 					source.getAppearanceStatus() == starterApp &&
 					source.getTransparency() == transparency)
 			return source;
 		else
 		{
 			DVAppearance dvApp = new DVAppearance(color,transparency);
 			dvApp.setAppearanceStatus(starterApp);
 			return dvApp;
 		}
 	}
 
 	private Color processColor(String tag, Color defaultColor)
 	{
 		List colorList = config.getList(tag);
 		String hexColor = config.getString(tag);
 		try
 		{
 			if (colorList.size() == 3)
 			{
 				int red = Integer.parseInt(colorList.get(0).toString());
 				int green = Integer.parseInt(colorList.get(1).toString());
 				int blue = Integer.parseInt(colorList.get(2).toString());
 				return new Color(red, green, blue);
 			}
 			else if(hexColor != null)
 			{
 				return Color.decode(hexColor);
 			}
 		}
 		catch (Exception e)
 		{
 			reportError(tag, "Badly formatted color setting", e.getMessage());
 		}
 
 		return defaultColor;
 	}
 
 	private DVAppearance.AppearanceStatus processStarterAppearance(
 					String tag, DVAppearance.AppearanceStatus sourceStarterApp)
 	{
 		try
 		{
 			String starter = config.getString(tag);
 			if (starter != null)
 			{
 				starter = starter.trim().toLowerCase();
 				if (starter.compareTo("solid") == 0)
 					return DVAppearance.AppearanceStatus.SOLID;
 				else if (starter.compareTo("line") == 0)
 					return DVAppearance.AppearanceStatus.LINE;
 				else if (starter.compareTo("transparent") == 0)
 					return DVAppearance.AppearanceStatus.TRANSPARENT;
 				else
 					throw new RuntimeException("Invalid fill-option");
 			}
 			else
 				return sourceStarterApp;
 		}
 		catch (Exception e)
 		{
 			reportError(tag, "Badly formatted transparency setting", e.getMessage());
 		}
 		return null;
 	}
 
 	private float processTransparency(String tag, float defaultTransp)
 	{
 		try
 		{
 			float transparency = config.getFloat(tag, defaultTransp * 100f);
 			if (transparency > 100)
 				throw new RuntimeException("Transparency cannot be higher than 100");
 			else if (transparency < 0)
 				throw new RuntimeException("Transparency cannot be a negative number");
 			return (transparency/100f);
 		}
 		catch (Exception e)
 		{
 			reportError(tag, "Badly formatted transparency setting", e.getMessage());
 		}
 		return defaultTransp;
 	}
 	// APPEARANCE PROCESSING - End
 	//***************************************************************************
 }
