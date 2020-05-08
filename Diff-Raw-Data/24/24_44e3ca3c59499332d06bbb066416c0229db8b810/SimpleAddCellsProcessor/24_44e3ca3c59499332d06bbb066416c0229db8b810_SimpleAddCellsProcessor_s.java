/*
 * Copyright (c) Sematext International
 * All Rights Reserved
 * <p/>
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
  */
 package com.sematext.hbase.hut;
 
 import org.apache.hadoop.hbase.client.Result;
 
 /**
 * Performs simple processing of records: just adds all columns from records to the final one.
  */
 public class SimpleAddCellsProcessor implements UpdateProcessor {
   @Override
   public void process(Iterable<Result> records, UpdateProcessingResult processingResult) {
     for (Result record : records) {
       processingResult.add(record.raw());
     }
   }
 }
