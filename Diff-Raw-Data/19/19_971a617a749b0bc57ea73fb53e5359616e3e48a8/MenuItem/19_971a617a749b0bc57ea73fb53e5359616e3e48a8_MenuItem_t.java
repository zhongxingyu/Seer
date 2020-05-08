 /*
  * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
  * Brandon Sutherlin, Scott Griffin
  * 
  * This software is released under the MIT license
  * (http://www.opensource.org/licenses/mit-license.php)
  * 
  * Created on Feb 18, 2012
  */
 package edu.cmu.sv.arinc838.ui.item;
 
 import edu.cmu.sv.arinc838.builder.SoftwareDefinitionFileBuilder;
 
 /**
  * Use the list to indicate the three possible options:
  *   1. actual sub menu items
  *   2. no-op, but ask this menu again
  *   3. pop up a level (exit)
  */
 public interface MenuItem {
 	public MenuItem[] execute (SoftwareDefinitionFileBuilder builder) throws Exception;
 	public String getPrompt ();
 	public String getHeader();
 }
