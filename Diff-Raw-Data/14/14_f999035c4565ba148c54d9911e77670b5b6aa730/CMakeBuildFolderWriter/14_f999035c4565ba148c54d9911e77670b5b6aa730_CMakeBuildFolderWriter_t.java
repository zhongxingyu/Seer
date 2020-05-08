 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2010 Ratouit Thomas                                    *
  *                                                                           *
  * This program is free software; you can redistribute it and/or modify it   *
  * under the terms of the GNU Lesser General Public License as published by  *
  * the Free Software Foundation; either version 3 of the License, or (at     *
  * your option) any later version.                                           *
  *                                                                           *
  * This program is distributed in the hope that it will be useful, but       *
  * WITHOUT ANY WARRANTY; without even the implied warranty of                *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   *
  * General Public License for more details.                                  *
  *                                                                           *
  * You should have received a copy of the GNU Lesser General Public License  *
  * along with this program; if not, write to the Free Software Foundation,   *
  * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA, or go to   *
  * http://www.gnu.org/copyleft/lesser.txt.                                   *
  *****************************************************************************/
 
 package screen.tools.sbs.cmake.writers;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import screen.tools.sbs.cmake.CMakeSegmentWriter;
 import screen.tools.sbs.cmake.CMakePack;
 import screen.tools.sbs.objects.ErrorList;
 import screen.tools.sbs.objects.GlobalSettings;
 import screen.tools.sbs.utils.FieldString;
import screen.tools.sbs.utils.FieldBuildType.Type;
 
 /**
  * set LIBRARY_OUTPUT_PATH or EXECUTABLE_OUTPUT_PATH 
  * <pre>
  * SET(LIBRARY_OUTPUT_PATH C:/Users/Thanh/Documents/Developments/SbsRepository/Screen/Main/0.1.0/lib/MingW/Release)
  * </pre>
  *
  * @author Ratouit Thomas
  *
  */
 public class CMakeBuildFolderWriter implements CMakeSegmentWriter{
 	/**
 	 * @see screen.tools.sbs.cmake.CMakeSegmentWriter#write(screen.tools.sbs.cmake.CMakePack, java.io.Writer)
 	 */
 	public void write(CMakePack cmakePack, Writer cmakeListsWriter)
 			throws IOException {
 		ErrorList err = GlobalSettings.getGlobalSettings().getErrorList();
 		
 		FieldString output = cmakePack.getOutputPath();
		if(output.isValid()){
			if(cmakePack.getBuildType().isValid()){
				if(cmakePack.getBuildType().get() != Type.EXECUTABLE)
					cmakeListsWriter.write("SET(LIBRARY_OUTPUT_PATH "+output.getString()+")\n");
				else
					cmakeListsWriter.write("SET(EXECUTABLE_OUTPUT_PATH "+output.getString()+")\n");
			}
			else
				err.addError("invalid build type");
		}
 		else
 			err.addError("invalid output path");
 	}
 }
