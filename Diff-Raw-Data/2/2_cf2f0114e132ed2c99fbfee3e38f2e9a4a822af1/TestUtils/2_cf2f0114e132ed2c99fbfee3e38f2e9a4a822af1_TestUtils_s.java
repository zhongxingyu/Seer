 /*
  * Copyright (c) 2011 Julien Nicoulaud <julien.nicoulaud@gmail.com>
  *
  * This file is part of idea-byteman.
  *
  * idea-byteman is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * idea-byteman is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with idea-byteman.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.nicoulaj.idea.byteman.test;
 
 import org.jetbrains.annotations.NonNls;
 
 import java.util.regex.Pattern;
 
 /**
  * Utils for tests.
  *
  * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
  * @since 0.1
  */
 public class TestUtils {
 
     /**
      * The path to the data used for running tests.
      */
     @NonNls
     public static final String TEST_RESOURCES_DIR = "src/test/resources";
 
     /**
      * The path to the Byteman scripts used for running tests.
      */
     @NonNls
     public static final String TEST_SCRIPTS_DIR = TEST_RESOURCES_DIR + "/scripts";
 
     /**
      * The pattern used for catching input test script files.
      */
    public static final Pattern TEST_SCRIPTS_NAME_PATTERN = Pattern.compile(".+.btm");
 
 }
