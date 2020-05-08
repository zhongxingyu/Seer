 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.emelz.transfer;
 
 import com.emelz.transfer.TransferUtil;
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 /**
  * Launcher
  * @author Eric Melz
  */
 class Launcher {
    //   public static final String NEAT_DATA_DIR = "/Users/eric/Documents/NeatQuick/sample_data/NeatDocs_2011_09_18.nrmlib";
    //   public static final String QUICKEN_DATA_DIR = "/Users/eric/Documents/NeatQuick/sample_data/Empty.quickendata";
    public static final String NEAT_DATA_DIR = null;
    public static final String QUICKEN_DATA_DIR = null;
    public static final Integer LIMIT = null;  // null means no limit
    //    public static final Integer LIMIT = new Integer(10);  // For testing, can limit # of neat transctions to, say, 10
     public static void main(String[] args) throws Exception {
        if (NEAT_DATA_DIR == null || QUICKEN_DATA_DIR == null)
            throw new IllegalArgumentException("You must set NEAT_DATA_DIR and QUICKEN_DATA_DIR.  See README for details");
         TransferUtil tu = new TransferUtil(NEAT_DATA_DIR, QUICKEN_DATA_DIR);
        tu.doTransfer(LIMIT);
     }
 }
