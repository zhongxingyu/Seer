 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-shell Project
 //
 // Import.java
 // Since: Jan 20, 2009
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.shell;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 
 import org.utgenome.format.bed.BEDDatabaseGenerator;
 import org.utgenome.format.fasta.FASTA2Db;
 import org.utgenome.format.silk.read.ReadDBBuilder;
 import org.utgenome.format.wig.WIGDatabaseGenerator;
 import org.xerial.util.log.Logger;
 import org.xerial.util.opt.Argument;
 import org.xerial.util.opt.Option;
 
 /**
  * import command
  * 
  * @author leo
  * 
  */
 // @Usage(command = "> utgb import", description = "import command")
 public class Import extends UTGBShellCommand {
 
 	private static Logger _logger = Logger.getLogger(Import.class);
 
	public static enum FileType {
 		AUTO, READ, BED, FASTA, WIG
 	}
 
 	@Option(symbol = "t", longName = "type", description = "specify the input file type: (AUTO, FASTA, READ, BED, WIG)")
 	private FileType fileType = FileType.AUTO;
 
 	@Argument(index = 0, required = false)
 	private String inputFilePath = null;
 
 	@Option(symbol = "o", longName = "output", varName = "DB FILE NAME", description = "output SQLite DB file name")
 	private String outputFileName;
 
 	@Option(symbol = "w", longName = "overwrite", description = "overwrite existing DB files")
 	private boolean overwriteDB = false;
 
 	@Override
 	public void execute(String[] args) throws Exception {
 
 		File input = null;
 
 		if (inputFilePath == null)
 			_logger.info("use STDIN for the input");
 		else {
 			_logger.info("input file: " + inputFilePath);
 			input = new File(inputFilePath);
 			if (!input.exists())
 				throw new UTGBShellException("file not found: " + inputFilePath);
 		}
 
 		_logger.info("file type: " + fileType);
 
 		if (outputFileName == null) {
 			// new File("db").mkdirs();
 
 			outputFileName = String.format("%s.sqlite", inputFilePath);
 			int count = 1;
 			if (!overwriteDB) {
 				while (new File(outputFileName).exists()) {
 					outputFileName = String.format("%s.sqlite.%d", inputFilePath, count++);
 				}
 			}
 		}
 
 		_logger.info("output file: " + outputFileName);
 
 		if (fileType == FileType.AUTO)
 			fileType = detectFileType(inputFilePath);
 
 		switch (fileType) {
 		case READ: {
 			ReadDBBuilder builder = new ReadDBBuilder(outputFileName);
 			if (input != null)
 				builder.build(input.toURL());
 			else
 				builder.build(new InputStreamReader(System.in));
 			break;
 		}
 		case BED: {
 			if (input != null)
 				BEDDatabaseGenerator.toSQLiteDB(new FileReader(input), outputFileName);
 			else
 				BEDDatabaseGenerator.toSQLiteDB(new InputStreamReader(System.in), outputFileName);
 			break;
 		}
 		case FASTA:
 			if (input != null)
 				FASTA2Db.main(new String[] { inputFilePath, "-o", outputFileName });
 			else
 				FASTA2Db.main(new String[] { "-o", outputFileName });
 			break;
 		case WIG:
 			if (input != null)
 				WIGDatabaseGenerator.toSQLiteDB(new FileReader(input), outputFileName);
 			else
 				WIGDatabaseGenerator.toSQLiteDB(new InputStreamReader(System.in), outputFileName);
 		case AUTO:
 		default: {
 			_logger.warn("file type (-t) must be specified");
 			break;
 		}
 		}
 
 	}
 
 	public static FileType detectFileType(String fileName) {
 		if (fileName.endsWith(".fa") || fileName.endsWith(".fasta"))
 			return FileType.FASTA;
 		else if (fileName.endsWith(".bed"))
 			return FileType.BED;
 		else if (fileName.endsWith(".wig"))
 			return FileType.WIG;
 
 		return FileType.AUTO;
 	}
 
 	@Override
 	public String name() {
 		return "import";
 	}
 
 	public String getOneLinerDescription() {
 		return "import a file and create a new database";
 	}
 
 }
