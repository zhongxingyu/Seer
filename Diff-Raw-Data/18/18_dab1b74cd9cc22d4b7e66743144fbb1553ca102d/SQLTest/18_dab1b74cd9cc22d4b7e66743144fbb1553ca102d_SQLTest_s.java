 /*
  * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
  * This cross-platform GIS is developed at French IRSTV institute and is able
  * to manipulate and create vector and raster spatial information. OrbisGIS
  * is distributed under GPL 3 license. It is produced  by the geo-informatic team of
  * the IRSTV Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
  *    Erwan BOCHER, scientific researcher,
  *    Thomas LEDUC, scientific researcher,
  *    Fernando GONZALEZ CORTES, computer engineer.
  *
  * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
  *
  * This file is part of OrbisGIS.
  *
  * OrbisGIS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OrbisGIS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult:
  *    <http://orbisgis.cerma.archi.fr/>
  *    <http://sourcesup.cru.fr/projects/orbisgis/>
  *
  * or contact directly:
  *    erwan.bocher _at_ ec-nantes.fr
  *    fergonco _at_ gmail.com
  *    thomas.leduc _at_ cerma.archi.fr
  */
 package org.gdms.sql.strategies;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.gdms.SourceTest;
 import org.gdms.data.DataSource;
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.DigestUtilities;
 import org.gdms.data.types.Constraint;
 import org.gdms.data.types.GeometryConstraint;
 import org.gdms.data.types.Type;
 import org.gdms.data.types.TypeFactory;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.data.values.ValueWriter;
 import org.gdms.driver.memory.ObjectMemoryDriver;
 
 import com.vividsolutions.jts.io.WKTReader;
 
 /**
  * @author Fernando Gonzalez Cortes
  */
 public class SQLTest extends SourceTest {
 	public static DataSource d;
 
 	public void testCaseInsensitiveness() throws Exception {
 		String name = super.getAnySpatialResource();
 		dsf.executeSQL("seLECt BuffER(" + super.getSpatialFieldName(name)
 				+ ", 20) From " + name);
 		dsf.executeSQL("selecT REGisteR('memory')");
 	}
 
 	private void testIsClause(String ds) throws Exception {
 		String fieldName = super.getContainingNullFieldNameFor(ds);
 		DataSource d = dsf.getDataSourceFromSQL("select * from " + ds
 				+ " where " + fieldName + " is not null;");
 		d.open();
 		int index = d.getFieldIndexByName(fieldName);
 		for (int i = 0; i < d.getRowCount(); i++) {
 			assertTrue(!d.isNull(i, index));
 		}
 		d.cancel();
 	}
 
 	public void testIsClause() throws Exception {
 		String[] resources = super.getResourcesWithNullValues();
 		for (String resource : resources) {
 			testIsClause(resource);
 		}
 	}
 
 	private void testBetweenClause(String ds) throws Exception {
 		String numericField = super.getNumericFieldNameFor(ds);
 		double low = super.getMinimumValueFor(ds, numericField);
 		double high = super.getMaximumValueFor(ds, numericField) + low / 2;
 		DataSource d = dsf.getDataSourceFromSQL("select * from " + ds
 				+ " where \"" + numericField + "\" between " + low + " and "
 				+ high + ";");
 		d.open();
 		for (int i = 0; i < d.getRowCount(); i++) {
 			double fieldValue = d.getDouble(i, numericField);
 			assertTrue((low <= fieldValue) && (fieldValue <= high));
 		}
 		d.cancel();
 	}
 
 	public void testBetweenClause() throws Exception {
 		String[] resources = super.getResourcesWithNumericField();
 		for (String resource : resources) {
 			testBetweenClause(resource);
 		}
 	}
 
 	private void testInClause(String ds) throws Exception {
 		String numericField = super.getNumericFieldNameFor(ds);
 		double low = super.getMinimumValueFor(ds, numericField);
 		double high = super.getMaximumValueFor(ds, numericField);
 		DataSource d = dsf.getDataSourceFromSQL("select * from " + ds
 				+ " where \"" + numericField + "\" in (" + low + ", " + high
 				+ ");");
 
 		d.open();
 		for (int i = 0; i < d.getRowCount(); i++) {
 			double fieldValue = d.getDouble(i, numericField);
 			assertTrue((low == fieldValue) || (fieldValue == high));
 		}
 		d.cancel();
 	}
 
 	public void testInClause() throws Exception {
 		String[] resources = super.getResourcesWithNumericField();
 		for (String resource : resources) {
 			testInClause(resource);
 		}
 	}
 
 	private void testAggregate(String ds) throws Exception {
 		String numericField = super.getNumericFieldNameFor(ds);
 		double low = super.getMinimumValueFor(ds, numericField);
 		double high = (super.getMaximumValueFor(ds, numericField) + low) / 2;
 
 		DataSource d = dsf.getDataSourceFromSQL("select count(\""
 				+ numericField + "\") from \"" + ds + "\" where \""
 				+ numericField + "\" < " + high + ";");
 
 		DataSource original = dsf.getDataSource(ds);
 		original.open();
 		int count = 0;
 		for (int i = 0; i < original.getRowCount(); i++) {
 			double fieldValue = original.getDouble(i, numericField);
 			if (fieldValue < high) {
 				count++;
 			}
 		}
 		original.cancel();
 
 		d.open();
 		assertTrue(count == d.getDouble(0, 0));
 		d.cancel();
 	}
 
 	public void testAggregate() throws Exception {
 		String[] resources = super.getResourcesWithNumericField();
 		for (String resource : resources) {
 			testAggregate(resource);
 		}
 	}
 
 	private void testTwoTimesTheSameAggregate(String ds) throws Exception {
 		String numericField = super.getNumericFieldNameFor(ds);
 		double low = super.getMinimumValueFor(ds, numericField);
 		double high = super.getMaximumValueFor(ds, numericField) + low / 2;
 
 		DataSource d = dsf.getDataSourceFromSQL("select count(\""
 				+ numericField + "\"), count(\"" + numericField + "\") from \""
 				+ ds + "\" where \"" + numericField + "\" < " + high + ";");
 
 		d.open();
 		assertTrue(equals(d.getFieldValue(0, 0), d.getFieldValue(0, 1)));
 		d.cancel();
 	}
 
 	public void testTwoTimesTheSameAggregate() throws Exception {
 		String[] resources = super.getResourcesWithNumericField();
 		for (String resource : resources) {
 			testTwoTimesTheSameAggregate(resource);
 		}
 	}
 
 	private void testOrderByAsc(String ds) throws Exception {
 		String fieldName = super.getNoPKFieldFor(ds);
 		String sql = "select * from \"" + ds + "\" order by \"" + fieldName
 				+ "\" asc;";
 
 		DataSource resultDataSource = dsf.getDataSourceFromSQL(sql);
 		resultDataSource.open();
 		int fieldIndex = resultDataSource.getFieldIndexByName(fieldName);
 		for (int i = 1; i < resultDataSource.getRowCount(); i++) {
 			Value v1 = resultDataSource.getFieldValue(i - 1, fieldIndex);
 			Value v2 = resultDataSource.getFieldValue(i, fieldIndex);
 			if (v1.getType() != Type.NULL) {
 				assertTrue(v1.lessEqual(v2).getAsBoolean());
 			}
 		}
 		resultDataSource.cancel();
 
 	}
 
 	public void testOrderByAsc() throws Exception {
 		String[] resources = super.getSmallResources();
 		for (String resource : resources) {
 			testOrderByAsc(resource);
 		}
 	}
 
 	private void testOrderByDesc(String ds) throws Exception {
 		String fieldName = super.getNoPKFieldFor(ds);
 		String sql = "select * from \"" + ds + "\" order by \"" + fieldName
 				+ "\" desc;";
 
 		DataSource resultDataSource = dsf.getDataSourceFromSQL(sql);
 		resultDataSource.open();
 		int fieldIndex = resultDataSource.getFieldIndexByName(fieldName);
 		for (int i = 1; i < resultDataSource.getRowCount(); i++) {
 			Value v1 = resultDataSource.getFieldValue(i - 1, fieldIndex);
 			Value v2 = resultDataSource.getFieldValue(i, fieldIndex);
 			if (v2.getType() != Type.NULL) {
 				assertTrue(v1.greaterEqual(v2).getAsBoolean());
 			}
 		}
 		resultDataSource.cancel();
 
 	}
 
 	public void testOrderByDesc() throws Exception {
 		String[] resources = super.getSmallResources();
 		for (String resource : resources) {
 			testOrderByDesc(resource);
 		}
 	}
 
 	private void testOrderByWithNullValues(String ds) throws Exception {
 		String fieldName = super.getContainingNullFieldNameFor(ds);
 		String sql = "select * from " + ds + " order by " + fieldName + " asc;";
 
 		DataSource resultDataSource = dsf.getDataSourceFromSQL(sql);
 		resultDataSource.open();
 		int fieldIndex = resultDataSource.getFieldIndexByName(fieldName);
 		boolean[] nullValues = new boolean[(int) resultDataSource.getRowCount()];
 		for (int i = 1; i < resultDataSource.getRowCount(); i++) {
 			if (resultDataSource.isNull(i, fieldIndex)) {
 				nullValues[i] = true;
 			} else {
 				nullValues[i] = false;
 				if (!resultDataSource.isNull(i - 1, fieldIndex)) {
 					Value v1 = resultDataSource
 							.getFieldValue(i - 1, fieldIndex);
 					Value v2 = resultDataSource.getFieldValue(i, fieldIndex);
 					assertTrue(v1.lessEqual(v2).getAsBoolean());
 				}
 			}
 		}
 		nullValues[0] = resultDataSource.isNull(0, fieldIndex);
 
 		for (int i = 1; i < nullValues.length - 1; i++) {
 			assertFalse("All null together", !nullValues[i]
 					&& nullValues[i - 1] && nullValues[i + 1]);
 		}
 		resultDataSource.cancel();
 	}
 
 	public void testOrderByWithNullValues() throws Exception {
 		String[] resources = super.getResourcesWithNullValues();
 		for (String resource : resources) {
 			testOrderByWithNullValues(resource);
 		}
 	}
 
 	public void testOrderByMultipleFields(String ds) throws Exception {
 		String[] fields = super.getFieldNames(super.getAnyNonSpatialResource());
 		String sql = "select * from " + ds + " order by " + fields[0] + ", "
 				+ fields[1] + " asc;";
 
 		DataSource resultDataSource = dsf.getDataSourceFromSQL(sql);
 		resultDataSource.open();
 		int fieldIndex1 = resultDataSource.getFieldIndexByName(fields[0]);
 		int fieldIndex2 = resultDataSource.getFieldIndexByName(fields[1]);
 		for (int i = 1; i < resultDataSource.getRowCount(); i++) {
 			Value v1 = resultDataSource.getFieldValue(i - 1, fieldIndex1);
 			Value v2 = resultDataSource.getFieldValue(i, fieldIndex1);
 			if (v1.less(v2).getAsBoolean()) {
 				assertTrue(true);
 			} else {
 				v1 = resultDataSource.getFieldValue(i - 1, fieldIndex2);
 				v2 = resultDataSource.getFieldValue(i, fieldIndex2);
 				assertTrue(v1.lessEqual(v2).getAsBoolean());
 			}
 		}
 		resultDataSource.cancel();
 
 	}
 
 	private void testDistinct(String ds) throws Exception {
 		String[] fields = super.getFieldNames(ds);
 		DataSource d = dsf.getDataSourceFromSQL("select distinct \""
 				+ fields[0] + "\" from \"" + ds + "\" ;");
 
 		d.open();
 		int fieldIndex = d.getFieldIndexByName(fields[0]);
 		Set<Value> valueSet = new HashSet<Value>();
 		for (int i = 0; i < d.getRowCount(); i++) {
 			assertTrue(!valueSet.contains(d.getFieldValue(i, fieldIndex)));
 			valueSet.add(d.getFieldValue(i, fieldIndex));
 		}
 		d.cancel();
 	}
 
 	public void testDistinct() throws Exception {
 		String[] resources = super.getResourcesWithRepeatedRows();
 		for (String resource : resources) {
 			testDistinct(resource);
 		}
 	}
 
 	public void testDistinctOnCommunesCase() throws Exception {
 		dsf.getSourceManager().register("communes",
 				new File("../../datas2tests/shp/bigshape2D/communes.shp"));
 		DataSource d = dsf
 				.getDataSourceFromSQL("select distinct \"STATUT\" from \"communes\" ;");
 
 		d.open();
 		int fieldIndex = d.getFieldIndexByName("STATUT");
 		Set<Value> valueSet = new HashSet<Value>();
 		for (int i = 0; i < d.getRowCount(); i++) {
 			assertTrue(!valueSet.contains(d.getFieldValue(i, fieldIndex)));
 			valueSet.add(d.getFieldValue(i, fieldIndex));
 		}
 		d.cancel();
 	}
 
 	private void testDistinctManyFields(String ds) throws Exception {
 		String[] fields = super.getFieldNames(ds);
 		DataSource d = dsf
 				.getDataSourceFromSQL("select distinct \"" + fields[0]
 						+ "\", \"" + fields[1] + "\" from \"" + ds + "\" ;");
 
 		d.open();
 		int fieldIndex1 = d.getFieldIndexByName(fields[0]);
 		int fieldIndex2 = d.getFieldIndexByName(fields[1]);
 		Set<Value> valueSet = new HashSet<Value>();
 		for (int i = 0; i < fields.length; i++) {
 			Value v1 = d.getFieldValue(i, fieldIndex1);
 			Value v2 = d.getFieldValue(i, fieldIndex2);
 			Value col = ValueFactory.createValue(new Value[] { v1, v2 });
 			assertTrue(!valueSet.contains(col));
 			valueSet.add(col);
 		}
 		d.cancel();
 	}
 
 	public void testDistinctManyFields() throws Exception {
 		String[] resources = super.getResourcesWithRepeatedRows();
 		for (String resource : resources) {
 			testDistinctManyFields(resource);
 		}
 	}
 
 	private void testDistinctAllFields(String ds) throws Exception {
 		String[] fields = super.getFieldNames(ds);
 		DataSource d = dsf.getDataSourceFromSQL("select distinct * from \""
 				+ ds + "\" ;");
 
 		d.open();
 		Set<Value> valueSet = new HashSet<Value>();
 		for (int i = 0; i < fields.length; i++) {
 			Value col = ValueFactory.createValue(d.getRow(i));
 			assertTrue(!valueSet.contains(col));
 			valueSet.add(col);
 		}
 		d.cancel();
 	}
 
 	public void testDistinctAllFields() throws Exception {
 		String[] resources = super.getResourcesWithRepeatedRows();
 		for (String resource : resources) {
 			testDistinctAllFields(resource);
 		}
 	}
 
 	public void testDistinctOnGeometricField() throws Exception {
 		final WKTReader wktr = new WKTReader();
 		final ObjectMemoryDriver driver = new ObjectMemoryDriver(
 				new String[] { "the_geom" }, new Type[] { TypeFactory
 						.createType(Type.GEOMETRY,
 								new Constraint[] { new GeometryConstraint(
 										GeometryConstraint.POINT) }) });
 		final String g1 = "POINT (0 0)";
 		driver
 				.addValues(new Value[] { ValueFactory
 						.createValue(wktr.read(g1)) });
 		driver
 				.addValues(new Value[] { ValueFactory
 						.createValue(wktr.read(g1)) });
 		dsf.getSourceManager().register("ds1", driver);
 		final DataSource dsResult = dsf
 				.getDataSourceFromSQL("select distinct the_geom from \"ds1\";");
 		dsResult.open();
 		assertTrue(dsResult.getRowCount() == 1);
 		dsResult.cancel();
 	}
 
 	private void testUnion(String ds) throws Exception {
 		d = dsf.getDataSourceFromSQL("(select * from \"" + ds
 				+ "\") union (select  * from \"" + ds + "\");");
 
 		d.open();
 		DataSource originalDS = dsf.getDataSource(ds);
 		originalDS.open();
 		for (int i = 0; i < originalDS.getRowCount(); i++) {
 			String[] fieldNames = d.getFieldNames();
 			Value[] row = d.getRow(0);
 			String sql = "select * from \"" + d.getName() + "\" where ";
 			String separator = "";
 			for (int j = 0; j < row.length; j++) {
 				sql += separator + "\"" + fieldNames[j] + "\"";
 				if (row[j].isNull()) {
 					sql += " is "
 							+ row[j]
 									.getStringValue(ValueWriter.internalValueWriter);
 				} else {
 					sql += "="
 							+ row[j]
 									.getStringValue(ValueWriter.internalValueWriter);
 				}
 				separator = " and ";
 			}
 
 			/*
 			 * We only test if there is an even number of equal rows
 			 */
 			DataSource testDS = dsf.getDataSourceFromSQL(sql);
 			testDS.open();
 			assertTrue((testDS.getRowCount() / 2) == (testDS.getRowCount() / 2.0));
 			testDS.cancel();
 		}
 		originalDS.cancel();
 		d.cancel();
 	}
 
 	public void testUnion() throws Exception {
 		String[] ds = super.getNonSpatialResourcesSmallerThan(100);
 		for (String string : ds) {
 			testUnion(string);
 		}
 	}
 
 	/**
 	 * Tests a simple select query
 	 *
 	 * @throws Throwable
 	 *             DOCUMENT ME!
 	 */
 	private void testSelect(String ds) throws Exception {
 		String numericField = super.getNumericFieldNameFor(ds);
 		double low = super.getMinimumValueFor(ds, numericField);
 		double average = super.getMaximumValueFor(ds, numericField) + low / 2;
 		DataSource d = dsf.getDataSourceFromSQL("select * from " + ds
 				+ " where \"" + numericField + "\"<" + average + ";");
 
 		d.open();
 		int fieldIndex = d.getFieldIndexByName(numericField);
 		for (int i = 0; i < d.getRowCount(); i++) {
 			assertTrue(d.getDouble(i, fieldIndex) < average);
 		}
 		d.cancel();
 	}
 
 	public void testSelect() throws Exception {
 		String[] resources = super.getResourcesWithNumericField();
 		for (String resource : resources) {
 			testSelect(resource);
 		}
 	}
 
 	public void testSecondaryIndependence() throws Exception {
 		DataSource d = dsf.getDataSourceFromSQL("select * from "
 				+ super.getAnyNonSpatialResource() + ";",
 				DataSourceFactory.EDITABLE);
 
 		DataSource d2 = dsf.getDataSourceFromSQL("select * from " + d.getName()
 				+ ";");
 
 		d.open();
 		for (int i = 0; i < d.getRowCount();) {
 			d.deleteRow(0);
 		}
 		d2.open();
 		assertTrue(!d.getAsString().equals(d2.getAsString()));
 		d2.getAsString();
 		d2.cancel();
 		d.cancel();
 	}
 
 	public void testGetDataSourceFactory() throws Exception {
 		DataSource d = dsf.getDataSourceFromSQL("select * from "
 				+ super.getAnyNonSpatialResource() + ";");
 
 		DataSource d2 = dsf.getDataSourceFromSQL("select * from " + d.getName()
 				+ ";");
 
 		assertTrue(dsf == d2.getDataSourceFactory());
 	}
 
 	public void testCreateAsSelect() throws Exception {
 		String source = super.getAnySpatialResource();
 		dsf.executeSQL("select register ('" + backupDir + "/"
 				+ "testCreate.shp', 'newShape') ");
 		dsf.executeSQL("create table \"newShape\" as select * from " + source);
 		DataSource newDs = dsf.getDataSource("newShape");
 		DataSource sourceDs = dsf.getDataSource(source);
 		newDs.open();
 		sourceDs.open();
 		byte[] d1 = DigestUtilities.getDigest(newDs);
 		byte[] d2 = DigestUtilities.getDigest(sourceDs);
 		assertTrue(DigestUtilities.equals(d1, d2));
 		newDs.cancel();
 		sourceDs.cancel();
 	}
 
 	public void testCreateAsUnion() throws Exception {
 		String source = super.getAnySpatialResource();
 		dsf.executeSQL("select register ('" + backupDir + "/"
 				+ "testCreate.shp', 'newShape') ");
 		dsf.executeSQL("create table \"newShape\" as " + source + " union "
 				+ source);
 		DataSource newDs = dsf.getDataSource("newShape");
 		DataSource sourceDs = dsf.getDataSource(source);
 		newDs.open();
 		sourceDs.open();
 		assertTrue(newDs.getRowCount() / 2.0 == sourceDs.getRowCount());
 		newDs.cancel();
 		sourceDs.cancel();
 	}
 
 	public void testAliasInFunction() throws Exception {
 		String dsName = super.getAnySpatialResource();
 		String alias = "myalias";
 		DataSource ds = dsf.getDataSourceFromSQL("select Buffer("
 				+ super.getSpatialFieldName(dsName) + ", 20) as " + alias
 				+ " from " + dsName);
 		ds.open();
 		assertTrue(ds.getFieldName(0).equals(alias));
 		ds.cancel();
 	}
 
 	public void testGroupByAndSumDouble() throws Exception {
 		dsf.getSourceManager().register("groupcsv",
 				new File(SourceTest.internalData + "groupby.csv"));
 		DataSource ds = dsf
 				.getDataSourceFromSQL("select count(category), category"
 						+ " from groupcsv group by category;");
 		ds.open();
 		assertTrue(ds.getRowCount() == 2);
 		ds.cancel();
 
 		ds = dsf
 				.getDataSourceFromSQL("select sum(string2double(id)), count(id), country, category"
 						+ " from groupcsv group by country, category order by country, category;");
 		ds.open();
 		assertTrue(ds.getRowCount() == 6);
 		assertTrue(ds.getInt(0, 0) == 5);
 		assertTrue(ds.getInt(1, 0) == 9);
 		assertTrue(ds.getInt(2, 0) == 8);
 		assertTrue(ds.getInt(3, 0) == 11);
 		assertTrue(ds.getInt(4, 0) == 3);
 		assertTrue(ds.getInt(5, 0) == 0);
 		ds.cancel();
 	}
 
 	public void testGroupByAliasedReference() throws Exception {
 		dsf.getSourceManager().register("groupcsv",
 				new File(SourceTest.internalData + "groupby.csv"));
 		DataSource ds = dsf.getDataSourceFromSQL("select category"
 				+ " from groupcsv g group by g.category;");
 		ds.open();
 		ds.getRow(0);
 		ds.cancel();
 	}
 
 	public void testLimitOffset() throws Exception {
 		String resource = super.getAnyNonSpatialResource();
 		testLimitOffset("select * from \"" + resource + "\" where true");
 		testLimit("select * from \"" + resource + "\" where true");
 		testOffset("select * from \"" + resource + "\" where true");
 
 		testLimitOffset("select * from \"" + resource + "\" ");
 		testLimit("select * from \"" + resource + "\" ");
 		testOffset("select * from \"" + resource + "\" ");
 
 		String stringField = super.getStringFieldFor(resource);
 		testLimitOffset("select \"" + stringField + "\" from \"" + resource
 				+ "\" group by \"" + stringField + "\"");
 		testLimit("select \"" + stringField + "\" from \"" + resource
 				+ "\" group by \"" + stringField + "\"");
 		testOffset("select \"" + stringField + "\" from \"" + resource
 				+ "\" group by \"" + stringField + "\"");
 	}
 
 	private void testLimitOffset(String sql) throws Exception {
 		String limitedSQL = sql + " limit 10 offset 10";
 		DataSource ds = dsf.getDataSourceFromSQL(limitedSQL);
 		DataSource original = dsf.getDataSourceFromSQL(sql);
 		ds.open();
 		original.open();
 		assertTrue(ds.getRowCount() == 10);
 		for (int i = 0; i < 10; i++) {
 			assertTrue(equals(ds.getRow(i), original.getRow(i + 10)));
 		}
 		ds.cancel();
 		original.cancel();
 	}
 
 	private void testLimit(String sql) throws Exception {
 		String limitedSQL = sql + " limit 10";
 		DataSource ds = dsf.getDataSourceFromSQL(limitedSQL);
 		DataSource original = dsf.getDataSourceFromSQL(sql);
 		ds.open();
 		original.open();
 		assertTrue(ds.getRowCount() == 10);
 		for (int i = 0; i < 10; i++) {
 			assertTrue(equals(ds.getRow(i), original.getRow(i)));
 		}
 		ds.cancel();
 		original.cancel();
 	}
 
 	private void testOffset(String sql) throws Exception {
 		String limitedSQL = sql + " offset 10";
 		DataSource ds = dsf.getDataSourceFromSQL(limitedSQL);
 		DataSource original = dsf.getDataSourceFromSQL(sql);
 		ds.open();
 		original.open();
 		assertTrue(ds.getRowCount() == original.getRowCount() - 10);
 		for (int i = 0; i < ds.getRowCount(); i++) {
 			assertTrue(equals(ds.getRow(i), original.getRow(i + 10)));
 		}
 		ds.cancel();
 		original.cancel();
 	}
 
 	public void testGroupAndOrderBy() throws Exception {
 		String[] res = getResourcesSmallerThan(1000);
 		for (String resource : res) {
 			testGroupAndOrderBy(resource);
 		}
 	}
 
 	private void testGroupAndOrderBy(String resource) throws Exception {
 		String field = super.getStringFieldFor(resource);
 		String sql = "select \"" + field + "\" from \"" + resource
 				+ "\" group by \"" + resource + "\".\"" + field
 				+ "\" order by \"" + resource + "\".\"" + field + "\"";
 		DataSource ds = dsf.getDataSourceFromSQL(sql);
 		ds.open();
 		assertTrue(ds.getMetadata().getFieldCount() == 1);
 		ds.cancel();
 	}
 
 	public void testNot() throws Exception {
 		String resource = super.getAnyNonSpatialResource();
 		String stringField = super.getStringFieldFor(resource);
 		String sql = "select * from \"" + resource + "\" where \""
 				+ stringField + "\" <> 'a'";
 		DataSource ds = dsf.getDataSourceFromSQL(sql);
 		ds.open();
 		long rc1 = ds.getRowCount();
 		ds.cancel();
 		sql = "select * from \"" + resource + "\" where not \"" + stringField
 				+ "\" <> 'a'";
 		ds = dsf.getDataSourceFromSQL(sql);
 		ds.open();
 		long rc2 = ds.getRowCount();
 		ds.cancel();
 		assertTrue(rc1 != rc2);
 	}
 
 	public void testNegativeValues() throws Exception {
 		String resource = super.getSmallResources()[0];
 		String sql = "select 1 from " + resource
 				+ " where -4 >= -128 and -4 < 0";
 		DataSource ds = dsf.getDataSourceFromSQL(sql);
 		ds.open();
 		assertTrue(ds.getRowCount() > 0);
 		ds.cancel();
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		setWritingTests(false);
 		super.setUp();
 	}
 
 }
