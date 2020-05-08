 /**
  * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
  * Copyright (C) 2013  Daniel Mapleson - TGAC
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  **/
 package uk.ac.tgac.conan.core.data;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import uk.ac.tgac.conan.core.util.XmlHelper;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Library {
 
     // **** Sub types ****
 	
 	public enum SeqOrientation {
 		FR,
 		RF,
 		FF,
 		RR;
 		
 		public static final SeqOrientation FORWARD_REVERSE = FR;
 		public static final SeqOrientation REVERSE_FORWARD = RF;
 		public static final SeqOrientation FORWARD_FORWARD = FF;
 		public static final SeqOrientation REVERSE_REVERSE = RR;
 		
 	}
 	
 	public enum Type {
 		OPE,
         PE,
 		MP,
 		SE;
 
         public static final Type OVERLAPPING_PAIRED_END = OPE;
         public static final Type PAIRED_END = PE;
 		public static final Type MATE_PAIR = MP;
 		public static final Type SINGLE_END = SE;
 	}
 
     public enum Phred {
         PHRED_33,
         PHRED_64;
     }
 	
 
     // **** Xml Key names ****
 
 	public static final String KEY_ELEM_FILES                   = "files";
     public static final String KEY_ELEM_FILE_PATH               = "path";
 
     public static final String KEY_ATTR_NAME                    = "name";
     public static final String KEY_ATTR_AVG_INSERT_SIZE         = "avg_insert_size";
 	public static final String KEY_ATTR_INSERT_ERROR_TOLERANCE  = "insert_err_tolerance";
 	public static final String KEY_ATTR_READ_LENGTH             = "read_length";
	public static final String KEY_ATTR_SEQ_ORIENTATION         = "orientation";
 	public static final String KEY_ATTR_TYPE                    = "type";
     public static final String KEY_ATTR_PHRED                   = "phred";
 
 
 
 
 
     // **** Class vars
 	
 	private String name;
 	private int averageInsertSize;
 	private double insertErrorTolerance;        // 0-1, 0- intolerant; 1-anything goes
 	private int readLength;
 	private SeqOrientation seqOrientation;
 	private Type type;
     private Phred phred;
 	private List<SeqFile> files;
 
 
     /**
      * Set default values
      */
     public Library() {
         this.name = "lib";
         this.averageInsertSize = 500;
         this.insertErrorTolerance = 0.2;
         this.readLength = 101;
         this.seqOrientation = SeqOrientation.FR;
         this.type = Type.PE;
         this.phred = Phred.PHRED_64;
         this.files = new ArrayList<SeqFile>();
     }
 
     /**
      * Set from Xml Element
      * @param ele
      */
     public Library(Element ele) {
 
         // Set defaults
         this();
 
         // Required values
         this.name = XmlHelper.getTextValue(ele, KEY_ATTR_NAME);
         this.averageInsertSize = XmlHelper.getIntValue(ele, KEY_ATTR_AVG_INSERT_SIZE);
         this.insertErrorTolerance = XmlHelper.getDoubleValue(ele, KEY_ATTR_INSERT_ERROR_TOLERANCE);
         this.readLength = XmlHelper.getIntValue(ele, KEY_ATTR_READ_LENGTH);
         this.seqOrientation = SeqOrientation.valueOf(XmlHelper.getTextValue(ele, KEY_ATTR_SEQ_ORIENTATION).toUpperCase());
         this.type = Type.valueOf(XmlHelper.getTextValue(ele, KEY_ATTR_TYPE).toUpperCase());
 
         // Optional
         String phredString = XmlHelper.getTextValue(ele, KEY_ATTR_PHRED);
        this.phred = phredString == null || phredString.isEmpty() ? null : Phred.valueOf(phredString.toUpperCase());
 
         // Some files (we test later if there are the correct number)
         Element fileElements = XmlHelper.getDistinctElementByName(ele, KEY_ELEM_FILES);
         NodeList nodes = fileElements.getElementsByTagName(KEY_ELEM_FILE_PATH);
         for(int i = 0; i < nodes.getLength(); i++) {
            String filePath = ((Element)nodes.item(i)).getFirstChild().getNodeValue();
            this.files.add(new SeqFile(filePath));
         }
     }
 
     public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getAverageInsertSize() {
 		return averageInsertSize;
 	}
 
 	public void setAverageInsertSize(int averageInsertSize) {
 		this.averageInsertSize = averageInsertSize;
 	}
 
 	public double getInsertErrorTolerance() {
 		return insertErrorTolerance;
 	}
 
 	public void setInsertErrorTolerance(double insertErrorTolerance) {
 		this.insertErrorTolerance = insertErrorTolerance;
 	}
 	
 	public int getReadLength() {
 		return readLength;
 	}
 
 	public void setReadLength(int readLength) {
 		this.readLength = readLength;
 	}
 	
 	public SeqOrientation getSeqOrientation() {
 		return seqOrientation;
 	}
 
 	public void setSeqOrientation(SeqOrientation seqOrientation) {
 		this.seqOrientation = seqOrientation;
 	}
 
 	public Type getType() {
 		return type;
 	}
 
 	public void setType(Type type) {
 		this.type = type;
 	}
 
     public Phred getPhred() {
         return phred;
     }
 
     public void setPhred(Phred phred) {
         this.phred = phred;
     }
 
    public List<SeqFile> getSeqFiles() {
         return files;
     }
 
    public List<File> getFiles() {

        List<File> fs = new ArrayList<File>();
        for(SeqFile seqFile : this.files) {
           fs.add(seqFile.getFile());
        }

        return fs;
    }

     public void setFiles(List<SeqFile> files) {
         this.files = files;
     }
 
     public File getFile1() {
         return this.files.get(0).getFile();
     }
 
     public SeqFile.FileType getFile1Type() {
         return this.files.get(0).getFileType();
     }
 
     public File getFile2() {
         return this.files.get(1).getFile();
     }
 
     public SeqFile.FileType getFile2Type() {
         return this.files.get(1).getFileType();
     }
 
     public void setFiles(String file1, String file2) {
         this.setFiles(new File(file1), file2 == null ? null : new File(file2));
     }
 
     public void setFiles(File file1, File file2) {
         this.files.clear();
 
         this.files.add(new SeqFile(file1));
 
         if (file2 != null) {
             this.files.add(new SeqFile(file2));
         }
     }
 
     public Library copy() {
         Library copy = new Library();
 
         copy.setName(this.name);
         copy.setAverageInsertSize(this.averageInsertSize);
         copy.setInsertErrorTolerance(this.insertErrorTolerance);
         copy.setReadLength(this.readLength);
         copy.setSeqOrientation(this.seqOrientation);
         copy.setType(this.type);
         copy.setPhred(this.phred);
 
        if (this.isPairedEnd()) {
            copy.setFiles(this.getFile1().getAbsolutePath(), this.getFile2().getAbsolutePath());
        }
        else {
            copy.setFiles(this.getFile1().getAbsolutePath(), null);
        }
 
         return copy;
     }
 
 
     public boolean isPairedEnd() {
         return this.type == Type.PE || this.type == Type.MP || this.type == Type.OPE;
     }
 
 }
 
 
