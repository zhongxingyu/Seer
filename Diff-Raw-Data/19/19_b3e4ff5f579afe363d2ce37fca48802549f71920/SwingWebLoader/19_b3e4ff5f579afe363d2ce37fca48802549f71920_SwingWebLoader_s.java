 /**
 *GUMA a simple math game for elementary school students
 *	Copyright (C) 2013-2014  Dimitrios Desyllas (pc_magas)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *Contact with me by main at thes address: pc_magas@yahoo.gr
 */
 
 package guma.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.beans.*;
 
 public class SwingWebLoader extends guma.net.WebGameLoader
 {
 	
 	/**
 	*Contructor Method
 	*@param url: the url of file
 	*@param size: The maximum size in Bytes
 	*/
 	public SwingWebLoader(String url,long size)
 	{
 		super(url,size);
 	}
 	
 	/**
 	*Constructor method
 	*@param url: the url of file on the web
 	*/
 	public SwingWebLoader(String url)
 	{
 		super(url);
 	}
 	
 	@Override
 	protected Object initProgress()
 	{
 		ProgressMonitor p=new ProgressMonitor(null,"Πρόοδος φόρτωσης παιχνιδιού από το διαδίκτυο","Πρόοδος",0,100);
 		return  (Object)p;
 	}
 	
 	@Override
 	protected void updateProgress(Object o,float progress)
 	{
 		ProgressMonitor p=(ProgressMonitor)o;
		p.setProgress((int)progress);
 	}
 }	
