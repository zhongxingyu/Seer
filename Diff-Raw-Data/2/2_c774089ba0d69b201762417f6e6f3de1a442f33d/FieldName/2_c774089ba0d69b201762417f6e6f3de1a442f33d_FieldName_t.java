 package com.codingstory.polaris.indexing;
 
 import com.google.common.collect.ImmutableSet;
 
 public class FieldName {
     public static final String FIELD_NAME = "FieldName";
    public static final String FIELD_TYPE_NAME = "FieldTypeName";
     public static final String FILE_CONTENT = "FileContent";
     public static final String FILE_NAME = "FileName";
     public static final String JAVA_DOC = "JavaDoc";
     public static final String KIND = "Kind";
     public static final String METHOD_NAME = "Method";
     public static final String OFFSET = "Offset";
     public static final String PACKAGE_NAME = "PackageName";
     public static final String TYPE_NAME = "TypeName";
     // TODO: method types
 
     public static final ImmutableSet<String> ALL_FIELDS = ImmutableSet.of(
             FIELD_NAME,
             FIELD_TYPE_NAME,
             FILE_CONTENT,
             FILE_NAME,
             JAVA_DOC,
             KIND,
             METHOD_NAME,
             OFFSET,
             PACKAGE_NAME,
             TYPE_NAME);
 }
