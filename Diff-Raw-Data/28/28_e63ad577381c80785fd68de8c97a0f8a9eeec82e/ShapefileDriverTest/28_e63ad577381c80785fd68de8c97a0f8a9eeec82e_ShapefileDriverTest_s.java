 /*
  * The GDMS library (Generic Datasources Management System)
  * is a middleware dedicated to the management of various kinds of
  * data-sources such as spatial vectorial data or alphanumeric. Based
  * on the JTS library and conform to the OGC simple feature access
  * specifications, it provides a complete and robust API to manipulate
  * in a SQL way remote DBMS (PostgreSQL, H2...) or flat files (.shp,
  * .csv...). GDMS is produced  by the geomatic team of the IRSTV
  * Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
  *    Erwan BOCHER, scientific researcher,
  *    Thomas LEDUC, scientific researcher,
  *    Fernando GONZALEZ CORTES, computer engineer.
  *
  * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
  *
  * This file is part of GDMS.
  *
  * GDMS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GDMS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with GDMS. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult:
  *    <http://orbisgis.cerma.archi.fr/>
  *    <http://sourcesup.cru.fr/projects/orbisgis/>
  *    <http://listes.cru.fr/sympa/info/orbisgis-developers/>
  *    <http://listes.cru.fr/sympa/info/orbisgis-users/>
  *
  * or contact directly:
  *    erwan.bocher _at_ ec-nantes.fr
  *    fergonco _at_ gmail.com
  *    thomas.leduc _at_ cerma.archi.fr
  */
 package org.gdms.drivers;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 
 import junit.framework.TestCase;
 
 import org.gdms.Geometries;
 import org.gdms.SourceTest;
 import org.gdms.data.BasicWarningListener;
 import org.gdms.data.DataSource;
 import org.gdms.data.DataSourceDefinition;
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.file.FileSourceCreation;
 import org.gdms.data.file.FileSourceDefinition;
 import org.gdms.data.metadata.DefaultMetadata;
 import org.gdms.data.object.ObjectSourceDefinition;
 import org.gdms.data.types.Constraint;
 import org.gdms.data.types.DimensionConstraint;
 import org.gdms.data.types.GeometryConstraint;
 import org.gdms.data.types.Type;
 import org.gdms.data.types.TypeFactory;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DriverException;
 import org.gdms.driver.DriverUtilities;
 import org.gdms.driver.memory.ObjectMemoryDriver;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 
 public class ShapefileDriverTest extends TestCase {
 	private DataSourceFactory dsf = new DataSourceFactory();
 
 	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
 	public void testOpenShapeWithDifferentCase() throws Exception {
 		DataSource ds = dsf.getDataSource(new File(SourceTest.internalData
 				+ "multipolygon2d.Shp"));
 		ds.open();
 		ds.cancel();
 	}
 
 	public void testBigShape() throws Exception {
 		DataSourceFactory dsf = new DataSourceFactory();
 		dsf.getSourceManager().register(
 				"big",
 				new FileSourceDefinition(new File(SourceTest.externalData
 						+ "shp/bigshape3D/point3D.shp")));
 		DataSource ds = dsf.getDataSource("big");
 		ds.open();
 		ds.cancel();
 	}
 
 	public void testSaveSQL() throws Exception {
 		DataSourceFactory dsf = new DataSourceFactory();
 		dsf.getSourceManager().register(
 				"shape",
 				new FileSourceDefinition(new File(SourceTest.externalData
 						+ "shp/mediumshape2D/landcover2000.shp")));
 
 		DataSource sql = dsf.getDataSourceFromSQL(
 				"select Buffer(the_geom, 20) from shape",
 				DataSourceFactory.DEFAULT);
 		DataSourceDefinition target = new FileSourceDefinition(new File(
 				SourceTest.backupDir, "outputtestSaveSQL.shp"));
 		dsf.getSourceManager().register("buffer", target);
 		dsf.saveContents("buffer", sql);
 
 		DataSource ds = dsf.getDataSource("buffer");
 		ds.open();
 		sql.open();
 		assertTrue(ds.getRowCount() == sql.getRowCount());
 		sql.cancel();
 		ds.cancel();
 	}
 
 	public void testSaveEmptyGeometries() throws Exception {
 		DataSourceFactory dsf = new DataSourceFactory();
 		ObjectMemoryDriver omd = new ObjectMemoryDriver(new String[] {
 				"the_geom", "id" }, new Type[] {
 				TypeFactory.createType(Type.GEOMETRY,
 						new Constraint[] { new GeometryConstraint(
 								GeometryConstraint.POINT) }),
 				TypeFactory.createType(Type.STRING) });
 		dsf.getSourceManager().register("obj", new ObjectSourceDefinition(omd));
 		DataSource ds = dsf.getDataSource("obj");
 		GeometryFactory gf = new GeometryFactory();
 		ds.open();
 		ds.insertFilledRow(new Value[] {
 				ValueFactory.createValue(gf
 						.createGeometryCollection(new Geometry[0])),
 				ValueFactory.createValue("0") });
 		ds.insertFilledRow(new Value[] { null, ValueFactory.createValue("1") });
 		DataSourceDefinition target = new FileSourceDefinition(new File(
 				SourceTest.backupDir, "outputtestSaveEmptyGeometries.shp"));
 		dsf.getSourceManager().register("buffer", target);
 		dsf.saveContents("buffer", ds);
 		String contents = ds.getAsString();
 		ds.cancel();
 
 		DataSource otherDs = dsf.getDataSource("buffer");
 		otherDs.open();
 		assertTrue(2 == otherDs.getRowCount());
 		assertTrue(otherDs.isNull(0, 0));
 		assertTrue(otherDs.isNull(1, 0));
 		assertTrue(otherDs.getAsString().equals(contents));
 		otherDs.cancel();
 	}
 
 	public void testSaveHeterogeneousGeometries() throws Exception {
 		DataSourceFactory dsf = new DataSourceFactory();
 		ObjectMemoryDriver omd = new ObjectMemoryDriver(new String[] { "id",
 				"geom" }, new Type[] { TypeFactory.createType(Type.STRING),
 				TypeFactory.createType(Type.GEOMETRY) });
 		dsf.getSourceManager().register("obj", new ObjectSourceDefinition(omd));
 		DataSourceDefinition target = new FileSourceDefinition(new File(
 				SourceTest.backupDir,
 				"outputtestSaveHeterogeneousGeometries.shp"));
 		DataSource ds = dsf.getDataSource("obj");
 		ds.open();
 		ds.insertFilledRow(new Value[] { ValueFactory.createValue("1"),
 				ValueFactory.createValue(Geometries.getPolygon()), });
 		ds.insertFilledRow(new Value[] { ValueFactory.createValue("0"),
 				ValueFactory.createValue(Geometries.getPoint()), });
 		try {
 			dsf.getSourceManager().register("buffer", target);
 			dsf.saveContents("buffer", ds);
 			assertTrue(false);
 		} catch (DriverException e) {
 		}
 		ds.cancel();
 		ds.open();
 		ds.insertFilledRow(new Value[] { ValueFactory.createValue("0"),
 				ValueFactory.createValue(Geometries.getPoint()), });
 		ds.insertFilledRow(new Value[] { ValueFactory.createValue("1"),
 				ValueFactory.createValue(Geometries.getPolygon()), });
 		try {
 			dsf.saveContents("buffer", ds);
 			assertTrue(false);
 		} catch (DriverException e) {
 		}
 		ds.cancel();
 	}
 
 	public void testSaveWrongType() throws Exception {
 		DataSourceFactory dsf = new DataSourceFactory();
 		BasicWarningListener listener = new BasicWarningListener();
 		dsf.setWarninglistener(listener);
 
 		ObjectMemoryDriver omd = new ObjectMemoryDriver(new String[] { "id",
 				"geom" }, new Type[] {
 				TypeFactory.createType(Type.INT),
 				TypeFactory.createType(Type.GEOMETRY,
 						new Constraint[] { new GeometryConstraint(
 								GeometryConstraint.POLYGON) }) });
 		dsf.getSourceManager().register("obj", new ObjectSourceDefinition(omd));
 		DataSourceDefinition target = new FileSourceDefinition(new File(
 				SourceTest.backupDir, "outputtestSaveWrongType.shp"));
 		DataSource ds = dsf.getDataSource("obj");
 		ds.open();
 		ds.insertFilledRow(new Value[] { ValueFactory.createValue("1"),
 				ValueFactory.createValue(Geometries.getPolygon()), });
 		dsf.getSourceManager().register("buffer", target);
 		dsf.saveContents("buffer", ds);
 		assertTrue(listener.warnings.size() == 1);
 		ds.cancel();
 	}
 
 	public void testFieldNameTooLong() throws Exception {
 		DataSourceFactory dsf = new DataSourceFactory();
 		BasicWarningListener listener = new BasicWarningListener();
 		dsf.setWarninglistener(listener);
 
 		DefaultMetadata m = new DefaultMetadata();
 		m.addField("thelongernameintheworld", Type.STRING);
 		m.addField("", Type.GEOMETRY,
 				new Constraint[] { new GeometryConstraint(
 						GeometryConstraint.POLYGON) });
 		File shpFile = new File(SourceTest.backupDir,
 				"outputtestFieldNameTooLong.shp");
 		if (shpFile.exists()) {
 			assertTrue(shpFile.delete());
 		}
 		dsf.createDataSource(new FileSourceCreation(shpFile, m));
 		assertTrue(listener.warnings.size() == 1);
 	}
 
 	public void testNullStringValue() throws Exception {
 		DataSourceFactory dsf = new DataSourceFactory();
 		BasicWarningListener listener = new BasicWarningListener();
 		dsf.setWarninglistener(listener);
 
 		DefaultMetadata m = new DefaultMetadata();
 		m.addField("string", Type.STRING);
 		m.addField("int", Type.INT);
 		m.addField("", Type.GEOMETRY,
 				new Constraint[] { new GeometryConstraint(
 						GeometryConstraint.POLYGON) });
 		File shpFile = new File(SourceTest.backupDir,
 				"outputtestNullStringValue.shp");
 		if (shpFile.exists()) {
 			assertTrue(shpFile.delete());
 		}
 		dsf.createDataSource(new FileSourceCreation(shpFile, m));
 		DataSource ds = dsf.getDataSource(shpFile);
 		ds.open();
 		ds.insertEmptyRow();
 		ds.setString(0, "string", null);
 		ds.setFieldValue(0, ds.getFieldIndexByName("int"), null);
 		ds.commit();
 		ds.open();
 		assertTrue(ds.getString(0, "string").equals(" "));
 		assertTrue(ds.getInt(0, "int") == 0);
 		assertTrue(listener.warnings.size() == 0);
 	}
 
 	public void test3DReadWrite() throws Exception {
 		DataSourceFactory dsf = new DataSourceFactory();
 
 		DefaultMetadata m = new DefaultMetadata();
 		m.addField("thelongernameintheworld", Type.STRING);
 		m.addField("", Type.GEOMETRY, new Constraint[] {
 				new GeometryConstraint(GeometryConstraint.POINT),
 				new DimensionConstraint(3) });
 		File shpFile = new File(SourceTest.backupDir,
 				"outputtest3DReadWrite.shp");
 		if (shpFile.exists()) {
 			assertTrue(shpFile.delete());
 		}
 		dsf.createDataSource(new FileSourceCreation(shpFile, m));
 		DataSource ds = dsf.getDataSource(shpFile);
 		ds.open();
 		ds.insertEmptyRow();
 		GeometryFactory gf = new GeometryFactory();
 		Point point = gf.createPoint(new Coordinate(0, 0, 0));
 		ds.setFieldValue(0, 0, ValueFactory.createValue(point));
 		ds.commit();
 		ds.open();
 		Geometry point2 = ds.getFieldValue(0, 0).getAsGeometry();
 		ds.cancel();
 		assertTrue(point.equals(point2));
 	}
 
 	public void testWrongTypeForDBF() throws Exception {
 		DataSourceFactory dsf = new DataSourceFactory();
 
 		DefaultMetadata m = new DefaultMetadata();
 		m.addField("id", Type.TIMESTAMP);
 		m.addField("", Type.GEOMETRY, new Constraint[] {
 				new GeometryConstraint(GeometryConstraint.POINT),
 				new DimensionConstraint(3) });
 		File shpFile = new File(SourceTest.backupDir,
 				"outputtestWrongTypeForDBF.shp");
 		if (shpFile.exists()) {
 			assertTrue(shpFile.delete());
 		}
 		try {
 			dsf.createDataSource(new FileSourceCreation(shpFile, m));
 			assertTrue(false);
 		} catch (DriverException e) {
 		}
 	}
 
 	public void testAllTypes() throws Exception {
 		DataSourceFactory dsf = new DataSourceFactory();
 		BasicWarningListener listener = new BasicWarningListener();
 		dsf.setWarninglistener(listener);
 
 		DefaultMetadata m = new DefaultMetadata();
 		m.addField("the_geom", Type.GEOMETRY, new Constraint[] {
 				new GeometryConstraint(GeometryConstraint.POINT),
 				new DimensionConstraint(3) });
 		m.addField("f1", Type.BOOLEAN);
 		m.addField("f2", Type.BYTE);
 		m.addField("f3", Type.DATE);
 		m.addField("f4", Type.DOUBLE);
 		m.addField("f5", Type.FLOAT);
 		m.addField("f6", Type.INT);
 		m.addField("f7", Type.LONG);
 		m.addField("f8", Type.SHORT);
 		m.addField("f9", Type.STRING);
 
 		File shpFile = new File(SourceTest.backupDir, "outputtestAllTypes.shp");
 		if (shpFile.exists()) {
 			assertTrue(shpFile.delete());
 		}
 		dsf.createDataSource(new FileSourceCreation(shpFile, m));
 		DataSource ds = dsf.getDataSource(shpFile);
 		ds.open();
 		assertTrue(m.getFieldType(0).getTypeCode() == Type.GEOMETRY);
 		assertTrue(m.getFieldType(1).getTypeCode() == Type.BOOLEAN);
 		assertTrue(m.getFieldType(2).getTypeCode() == Type.BYTE);
 		assertTrue(m.getFieldType(3).getTypeCode() == Type.DATE);
 		assertTrue(m.getFieldType(4).getTypeCode() == Type.DOUBLE);
 		assertTrue(m.getFieldType(5).getTypeCode() == Type.FLOAT);
 		assertTrue(m.getFieldType(6).getTypeCode() == Type.INT);
 		assertTrue(m.getFieldType(7).getTypeCode() == Type.LONG);
 		assertTrue(m.getFieldType(8).getTypeCode() == Type.SHORT);
 		assertTrue(m.getFieldType(9).getTypeCode() == Type.STRING);
 		ds.commit();
 
 		assertTrue(listener.warnings.size() == 0);
 	}
 
 	// SEE THE GT BUG REPORT :
 	// http://jira.codehaus.org/browse/GEOT-1268
 
 	public void testReadAndWriteDBF() throws Exception {
 		File file = new File(SourceTest.internalData + "alltypes.dbf");
 		File backup = new File(SourceTest.internalData + "backup/alltypes.dbf");
 		DriverUtilities.copy(file, backup);
 		DataSource ds = dsf.getDataSource(backup);
 		for (int i = 0; i < 2; i++) {
 			ds.open();
 			ds.insertFilledRow(new Value[] { ValueFactory.createValue(1),
 					ValueFactory.createValue(23.4d),
 					ValueFactory.createValue(2556),
 					ValueFactory.createValue("sadkjsr"),
 					ValueFactory.createValue(sdf.parse("1980-7-23")),
 					ValueFactory.createValue(true) });
 			ds.commit();
 		}
 		ds.open();
 		String content = ds.getAsString();
 		ds.commit();
 		ds.open();
 		assertTrue(content.equals(ds.getAsString()));
 		ds.commit();
 	}
 
 	public void testReadAndWriteSHP() throws Exception {
 		File file = new File(SourceTest.internalData + "alltypes.shp");
 		File backup1 = new File(SourceTest.internalData + "backup/alltypes.shp");
 		DriverUtilities.copy(file, backup1);
 		File backup = backup1;
 		file = new File(SourceTest.internalData + "alltypes.shx");
 		File backup2 = new File(SourceTest.internalData + "backup/alltypes.shx");
 		DriverUtilities.copy(file, backup2);
 		file = new File(SourceTest.internalData + "alltypes.dbf");
 		File backup3 = new File(SourceTest.internalData + "backup/alltypes.dbf");
 		DriverUtilities.copy(file, backup3);
 		DataSource ds = dsf.getDataSource(backup);
 		GeometryFactory gf = new GeometryFactory();
 		for (int i = 0; i < 2; i++) {
 			ds.open();
 			ds.insertFilledRow(new Value[] {
 					ValueFactory.createValue(gf.createPoint(new Coordinate(10,
 							10))), ValueFactory.createValue(1),
 					ValueFactory.createValue(23.4d),
 					ValueFactory.createValue(2556),
 					ValueFactory.createValue("sadkjsr"),
 					ValueFactory.createValue(sdf.parse("1980-7-23")),
 					ValueFactory.createValue(true) });
 			ds.commit();
 		}
 		ds.open();
 		String content = ds.getAsString();
 		ds.commit();
 		ds.open();
 		assertTrue(content.equals(ds.getAsString()));
 		ds.commit();
 	}
 
 }
