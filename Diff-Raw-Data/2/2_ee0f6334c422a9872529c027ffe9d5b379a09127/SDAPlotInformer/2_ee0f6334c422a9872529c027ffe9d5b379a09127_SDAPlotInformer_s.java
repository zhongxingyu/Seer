 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis;
 
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
 
 public class SDAPlotInformer {
 
 	private static final String IMAGE_EXPLORER_DIRECTORY = ".";
	private static final String IMAGE_EXPLORER_VIEW = "ImageExplorer View";
 	private static final String IMAGE_EXPLORER_HOST = "localhost";
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		System.out.println("SDA Plot Informer called");
 		String directoryToMonitor = IMAGE_EXPLORER_DIRECTORY;
 		try {
 			directoryToMonitor = args[0];
 		} catch (Exception e) {
 			System.out.println("No Direcotry Specified");
 		}
 		
 		System.out.println("Directory to Monitor is " + directoryToMonitor);
 		
 		
 		String viewToUpdate = IMAGE_EXPLORER_VIEW;
 		try {
 			viewToUpdate = args[1];
 		} catch (Exception e) {
 			System.out.println("No Plot View Specified");
 		}
 		
 		System.out.println("View to update is " + viewToUpdate);
 		
 		
 		String hostLocation = IMAGE_EXPLORER_HOST;
 		try {
 			hostLocation = args[2];
 		} catch (Exception e) {
 			System.out.println("No Host Location defined");
 		}
 		
 		System.out.println("Host to update is " + hostLocation);
 		
 		PlotService plotServer = PlotServiceProvider.getPlotService(hostLocation);
 		
 		if (plotServer != null) {		
 			
 			GuiBean guiBean = new GuiBean();
 			guiBean.put(GuiParameters.IMAGEGRIDLIVEVIEW, directoryToMonitor);
 			try {
 				plotServer.updateGui(viewToUpdate, guiBean);
 			} catch (Exception e) {
 				System.err.println("Cannot communicate with the PlotServer");
 				return;
 			}
 	
 		}
 		System.out.println("Update provided");
 		return;
 
 	}
 
 }
