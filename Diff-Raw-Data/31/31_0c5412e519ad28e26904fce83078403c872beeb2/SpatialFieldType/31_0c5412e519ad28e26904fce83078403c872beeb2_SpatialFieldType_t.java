 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.solr.spatial;
 
 import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queries.function.ValueSource;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.spatial.base.context.SpatialContext;
 import org.apache.lucene.spatial.base.context.SpatialContextProvider;
 import org.apache.lucene.spatial.base.query.SpatialArgsParser;
 import org.apache.lucene.spatial.base.shape.Shape;
 import org.apache.lucene.spatial.strategy.SpatialFieldInfo;
 import org.apache.lucene.spatial.strategy.SpatialStrategy;
 import org.apache.solr.common.SolrException;
 import org.apache.solr.response.TextResponseWriter;
 import org.apache.solr.schema.FieldType;
 import org.apache.solr.schema.IndexSchema;
 import org.apache.solr.schema.SchemaField;
 import org.apache.solr.search.QParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Map;
 
 public abstract class SpatialFieldType<T extends SpatialFieldInfo> extends FieldType
 {
   protected final Logger log = LoggerFactory.getLogger( getClass() );
 
   protected SpatialContext ctx;
   protected SpatialArgsParser argsParser;
 
   protected boolean ignoreIncompatibleGeometry = false;
   protected SpatialStrategy<T> spatialStrategy;
   protected double distPrec;
 
 
   @Override
   protected void init(IndexSchema schema, Map<String, String> args) {
     super.init(schema, args);
     String v = args.remove( "ignoreIncompatibleGeometry" );
     if( v != null ) {
       ignoreIncompatibleGeometry = Boolean.valueOf( v );
     }
     v = args.remove("distPrec");
     if (v != null)
       distPrec = Double.parseDouble(v);
 
     //args.setDistPrecision(readDouble(aa.remove("distPrec")));
 
    ctx = SpatialContextProvider.getContext();//TODO use a field specific ctx; not a global singleton
     argsParser = new SpatialArgsParser();
   }
 
   protected abstract T getFieldInfo( SchemaField field );
 
   //--------------------------------------------------------------
   // Indexing
   //--------------------------------------------------------------
 
   @Override
  public final IndexableField createField(SchemaField field, Object val, float boost) {
     Shape shape = (val instanceof Shape)?((Shape)val): ctx.readShape( val.toString() );
     if( shape == null ) {
       log.warn( "null shape for input: "+val );
       return null;
     }
     return spatialStrategy.createField(getFieldInfo(field), shape, field.indexed(), field.stored());
   }
 
   @Override
  public final IndexableField[] createFields(SchemaField field, Object val, float boost) {
     Shape shape = (val instanceof Shape)?((Shape)val): ctx.readShape( val.toString() );
     if( shape == null ) {
       log.warn( "null shape for input: "+val );
       return null;
     }
     return spatialStrategy.createFields(getFieldInfo(field), shape, field.indexed(), field.stored());
   }
 
   @Override
   public final boolean isPolyField() {
     return spatialStrategy.isPolyField();
   }
 
   //--------------------------------------------------------------
   // Query Support
   //--------------------------------------------------------------
 
   @Override
   public Query getRangeQuery(QParser parser, SchemaField field, String part1, String part2, boolean minInclusive, boolean maxInclusive) {
    //TODO assume a pair of points and bounding-box query
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Range query not supported on SpatialField: " + field.getName());
   }
 
   @Override
  public ValueSource getValueSource(SchemaField field, QParser parser) {
    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "ValueSource not supported on SpatialField: " + field.getName());
   }
 
  @Override
  public Query getFieldQuery(QParser parser, SchemaField field, String externalVal) {
    return spatialStrategy.makeQuery(argsParser.parse( externalVal, ctx), getFieldInfo(field));
   }
 
   @Override
   public void write(TextResponseWriter writer, String name, IndexableField f) throws IOException {
     writer.writeStr(name, f.stringValue(), true);
   }
 
   @Override
   public SortField getSortField(SchemaField field, boolean top) {
     throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Sorting not supported on SpatialField: " + field.getName());
   }
 }
 
 
