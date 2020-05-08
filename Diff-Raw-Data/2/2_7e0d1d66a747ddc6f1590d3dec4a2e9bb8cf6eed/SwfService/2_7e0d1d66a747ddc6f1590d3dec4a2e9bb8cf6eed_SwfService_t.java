 /*
  * Copyright (c) NASK, NCSC
  * 
  * This file is part of HoneySpider Network 2.0.
  * 
  * This is a free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pl.nask.hsn2.service;
 
 import pl.nask.hsn2.GenericService;
 import pl.nask.swftool.cvetool.CveTool;
 
 public final class SwfService {
 	private SwfService() {}
 
 	public static void main(String[] args) throws InterruptedException {
 		SwfCommandLineParams cmd = parseArguments(args);
 
 		CveTool tool = initCveTool(cmd.getPluginsPath());
		GenericService service = new GenericService(new SwfTaskFactory(tool), cmd.getMaxThreads(), cmd.getRbtCommonExchangeName(), cmd.getRbtNotifyExchangeName());
 
 		cmd.applyArguments(service);
 		service.run();
 	}
 
 	private static CveTool initCveTool(String pluginsDirectory) {
 		CveTool ct = new CveTool();
 		ct.loadPlugins(pluginsDirectory);
 		ct.printPluginsInfo();
 		ct.bulidPluginsDistributor();
 
 		return ct;
 	}
 
 	private static SwfCommandLineParams parseArguments(String[] args) {
 		SwfCommandLineParams params = new SwfCommandLineParams();
 		params.parseParams(args);
 
 		return params;
 	}
 }
