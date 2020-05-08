 package com.andrewma.vision.models;
 
 import com.andrewma.vision.database.core.DbDataType;
 import com.andrewma.vision.database.core.annotations.Column;
 import com.andrewma.vision.database.core.annotations.PrimaryKey;
 
 /**
  * When glasses are added to the inventory in batches, this is used to keep
  * track of which glasses were added into which batch
  * 
  * @author ajma
  * 
  */
 public class Batch {
 	@PrimaryKey
 	public int BatchId;
 
 	/**
 	 * Name of person working on this batch
 	 */
 	@Column(dataType = DbDataType.TEXT)
 	public String Name;
 
 	/**
 	 * Comma-delimited list of {@link Glasses#GlassesId} that are in this batch
 	 */
 	@Column(dataType = DbDataType.TEXT)
 	public String Glasses;
 }
