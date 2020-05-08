 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
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
 // utgb-core Project
 //
 // Database.java
 // Since: May 25, 2010
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.bio;
 
 import java.io.Serializable;
 
 /**
  * Database locator.
  * 
  * @author leo
  * 
  */
 public class GenomeDB implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public static enum DBType {
 		AUTO, BED, SAM, BAM, FASTA, DAS, CytoBand;
 
 		private DBType() {
 		}
 
 		public static String[] getDBTypeList() {
 			DBType[] values = DBType.values();
 			String[] dbTypes = new String[values.length];
 			for (int i = 0; i < values.length; ++i) {
 				dbTypes[i] = values[i].name();
 			}
 			return dbTypes;
 		}
 
 	}
 
 	/**
 	 * database path.
 	 * 
 	 * When the path starts with "local:", it searches for locally stored database files.
 	 * 
 	 * 
 	 */
 	public String path;
 
 	/**
 	 * reference sequence name to which this genome DB is associated
 	 */
 	public String ref;
 
 	/**
 	 * database type
 	 */
 	public DBType type = DBType.AUTO;
 
 	public GenomeDB() {
 	}
 
 	protected GenomeDB(GenomeDB db) {
 		this(db.type, db.path, db.ref);
 	}
 
 	public GenomeDB(String path, String refSeq) {
 		this(DBType.AUTO, path, refSeq);
 	}
 
 	public GenomeDB(DBType type, String path, String ref) {
 		if (type == null)
 			type = DBType.AUTO;
 		if (path == null)
 			throw new NullPointerException("Database ID is null");
 		if (ref == null)
 			throw new NullPointerException("Reference is null");
 
 		this.type = type;
 		this.path = path;
 		this.ref = ref;
 	}
 
 	@Override
 	public String toString() {
 		return "db(path:" + path + ", type:" + type + ", ref:" + ref + ")";
 	}
 
 	public DBType resolveDBType() {
 		return resolveDBType(this);
 	}
 
 	/**
 	 * Resolve the db type when the db type is AUTO.
 	 * 
 	 * @param db
 	 * @return the resolved data type, or null if no appropriate db type is found
 	 */
 	public static DBType resolveDBType(GenomeDB db) {
 		if (db.type == DBType.AUTO) {
			return resolveDBType(db.path);
 		}
 		else
 			return db.type;
 	}
 
 	public static DBType resolveDBType(String path) {
 		if (path.endsWith(".sam"))
 			return DBType.SAM;
 		else if (path.endsWith(".bam"))
 			return DBType.BAM;
 		else if (path.endsWith(".bed"))
 			return DBType.BED;
		else if (path.endsWith(".fa") || path.endsWith(".fasta"))
 			return DBType.FASTA;
 		else if (path.endsWith(".cytoband"))
 			return DBType.CytoBand;
 		return null;
 	}
 
 }
