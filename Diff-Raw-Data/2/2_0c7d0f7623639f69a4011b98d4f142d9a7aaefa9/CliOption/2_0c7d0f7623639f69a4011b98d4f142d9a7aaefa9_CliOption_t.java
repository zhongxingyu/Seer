 /*
  * Copyright 2011 Gregory P. Moyer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.syphr.mythtv.monitor.cli;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 
 public enum CliOption
 {
     HELP(new Option("h", "help", false, "show this usage text")),
    QUIET(new Option("q", "quiet", false, "suppress all console output")),
     LOG_FILE(new Option("f", "logfile", true, "log file")),
     LOG_LEVEL(new Option("l", "loglevel", true, "log verbosity level")),
     LOG_CONFIG(new Option("g",
                           "logconfig",
                           true,
                           "logging config file (this will affect the other logging options)")),
     CONFIG(new Option("c", "configdir", true, "configuration directory"));
 
     private final Option option;
 
     private CliOption(Option option)
     {
         this.option = option;
     }
 
     public Option getOption()
     {
         return option;
     }
 
     public boolean hasOption(CommandLine cl)
     {
         return cl.hasOption(getOption().getOpt());
     }
 
     public String getValue(CommandLine cl)
     {
         return cl.getOptionValue(getOption().getOpt());
     }
 
     public static Options getOptions()
     {
         Options options = new Options();
 
         for (CliOption opt : values())
         {
             options.addOption(opt.getOption());
         }
 
         return options;
     }
 }
