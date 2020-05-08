 /*
  * #%L
  * Support code for the 3D Niche at the Wisconsin Institutes for Discovery.
  * %%
  * Copyright (C) 2012 Board of Regents of the University of Wisconsin-Madison.
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 2 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-2.0.html>.
  * #L%
  */
 
 package loci.niche;
 
 import imagej.command.DynamicCommand;
 import imagej.module.ItemIO;
 import imagej.plugin.Parameter;
 import imagej.plugin.Plugin;
 
 /**
  * Populates a MODS template for the WID 3D Niche system.
  * 
  * @author Jason Palmer
  * @author Curtis Rueden
  */
 @Plugin(menuPath = "Plugins>WID>MODS Form", headless = true)
 public class MODSForm extends DynamicCommand {
 
 	// -- Constants --
 
	private static final String TEMPLATE = "/loci/niche/widmods.template";
 
 	// -- Parameters --
 
 	@Parameter(label = "Submission number", min = "1")
 	protected int submissionNumber;
 
 	@Parameter(label = "First name")
 	protected String firstName;
 
 	@Parameter(label = "Last name")
 	protected String lastName;
 
 	@Parameter(label = "Date acquired (in YYYY-MM-DD format)")
 	protected String date;
 
 	@Parameter(label = "Title of Data Set")
 	protected String title;
 
 	@Parameter(label = "Abstract of data set")
 	protected String Abstract;
 
 	@Parameter(label = "Keyword #1")
 	protected String kw1;
 
 	@Parameter(label = "Keyword #2")
 	protected String kw2;
 
 	@Parameter(label = "Keyword #3")
 	protected String kw3;
 
 	@Parameter(label = "Image width", min = "1")
 	protected int sizeX = 512;
 
 	@Parameter(label = "Image height", min = "1")
 	protected int sizeY = 512;
 
 	@Parameter(label = "Focal planes", min = "1")
 	protected int sizeZ = 1;
 
 	@Parameter(label = "Channel count", min = "1")
 	protected int sizeC = 1;
 
 	@Parameter(label = "Time points", min = "1")
 	protected int sizeT = 1;
 
 	@Parameter(label = "MODS", type = ItemIO.OUTPUT)
 	protected String output;
 
 	// -- Runnable methods --
 
 	@Override
 	public void run() {
 		final TemplateFiller templateFiller = new TemplateFiller();
 		output = templateFiller.fillTemplate(TEMPLATE, this);
 	}
 
 }
