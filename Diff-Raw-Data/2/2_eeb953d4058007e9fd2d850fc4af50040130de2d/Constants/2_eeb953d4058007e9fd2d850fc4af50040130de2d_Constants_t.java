 package com.edge.twitter_research.relevance_filter;
 
 
 public class Constants {
 
     public static final String LOG4J_PROPERTIES_FILE_PATH =
            System.getProperty("user.home") + "/twitter_research/relevance_filter/log4j.properties";
 
     public static final String POS_TAGGING_MODEL_FILE_PATH = "/model.20120919";
 
     public static final String TWEET_COLUMN_FAMILY_NAME = "tweet_object";
     public static final String TWEET_OBJECT_COLUMN_NAME = "tweet";
     public static final String TWEET_RELEVANCE_LABEL_COLUMN_NAME = "relevance_label";
 
     public static final String RELEVANT_RELEVANCE_LABEL = "topic-related";
     public static final String NOT_RELEVANT_RELEVANCE_LABEL = "not-topic-related";
     public static final String NOT_ENGLISH_RELEVANCE_LABEL = "not-english";
 
     public static final String ADDTIONAL_JARS_PATH_BENTO = "hdfs://localhost:8020/extraJars";
     public static final String ADDTIONAL_JARS_PATH_KIJI_CLUSTER = "hdfs://master:54310/user/hduser/extraJars";
 
 }
