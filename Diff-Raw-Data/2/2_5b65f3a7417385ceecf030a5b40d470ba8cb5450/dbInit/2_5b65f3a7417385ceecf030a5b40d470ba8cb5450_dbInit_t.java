 package nl.giantit.minecraft.giantshop.core.Tools.dbInit;
 
 import nl.giantit.minecraft.giantshop.GiantShop;
 import nl.giantit.minecraft.giantcore.database.Driver;
 import nl.giantit.minecraft.giantshop.core.Tools.dbInit.Updates.*;
 import nl.giantit.minecraft.giantcore.database.QueryResult;
 import nl.giantit.minecraft.giantcore.database.query.Column;
 import nl.giantit.minecraft.giantcore.database.query.CreateQuery;
 import nl.giantit.minecraft.giantcore.database.query.InsertQuery;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 
 public class dbInit {
 	
 	private Driver dbDriver;
 	private double curS = 1.0, curI = 1.1, curD = 1.2, curL = 1.1;
 
 	private void init() {
 		if(!this.dbDriver.tableExists("#__versions")) {
 			CreateQuery cQ = this.dbDriver.create("#__versions");
 			Column tN = cQ.addColumn("tableName");
 			tN.setDataType(Column.DataType.VARCHAR);
 			tN.setLength(100);
 			
 			Column v = cQ.addColumn("version");
 			v.setDataType(Column.DataType.DOUBLE);
 			v.setRawDefault("1.0");
 			
 			cQ.exec();
 			
 			GiantShop.log.log(Level.INFO, "Revisions table successfully created!");
 		}
 		
 		if(!this.dbDriver.tableExists("#__log")){
 			ArrayList<String> field = new ArrayList<String>();
 			field.add("tablename");
 			field.add("version");
 			
 			InsertQuery iQ = this.dbDriver.insert("#__versions");
 			iQ.addFields(field);
 			iQ.addRow();
 			iQ.assignValue("tablename", "log");
 			iQ.assignValue("version", "1.1", InsertQuery.ValueType.RAW);
 			iQ.exec();
 			
 			CreateQuery cQ = this.dbDriver.create("#__log");
 			Column id = cQ.addColumn("id");
 			id.setDataType(Column.DataType.INT);
 			id.setLength(3);
 			id.setAutoIncr();
 			id.setPrimaryKey();
 			
 			Column t = cQ.addColumn("type");
 			t.setDataType(Column.DataType.INT);
 			t.setLength(3);
 			t.setNull();
 			
 			Column u = cQ.addColumn("user");
 			u.setDataType(Column.DataType.VARCHAR);
 			u.setLength(100);
 			u.setNull();
 			
 			Column da = cQ.addColumn("data");
 			da.setDataType(Column.DataType.TEXT);
 			da.setNull();
 			
 			Column de = cQ.addColumn("date");
 			de.setDataType(Column.DataType.BIGINT);
 			de.setLength(50);
 			de.setRawDefault("0");
 			
 			cQ.exec();
 			
 			GiantShop.log.log(Level.INFO, "Logging table successfully created!");
 		}
 		
 		if(!this.dbDriver.tableExists("#__shops")) {
 			ArrayList<String> field = new ArrayList<String>();
 			field.add("tablename");
 			field.add("version");
 			
 			InsertQuery iQ = this.dbDriver.insert("#__versions");
 			iQ.addFields(field);
 			iQ.addRow();
 			iQ.assignValue("tablename", "shops");
 			iQ.assignValue("version", "1.0", InsertQuery.ValueType.RAW);
 			iQ.exec();
 			
 			CreateQuery cQ = this.dbDriver.create("#__shops");
 			Column id = cQ.addColumn("id");
 			id.setDataType(Column.DataType.INT);
 			id.setLength(3);
 			id.setAutoIncr();
 			id.setPrimaryKey();
 			
 			Column t = cQ.addColumn("type");
 			t.setDataType(Column.DataType.INT);
 			t.setLength(3);
 			t.setNull();
 			
 			Column u = cQ.addColumn("name");
 			u.setDataType(Column.DataType.VARCHAR);
 			u.setLength(100);
 			
 			Column w = cQ.addColumn("world");
 			w.setDataType(Column.DataType.VARCHAR);
 			w.setLength(100);
 			w.setNull();
 			
 			Column lmX = cQ.addColumn("LocMinX");
 			lmX.setDataType(Column.DataType.DOUBLE);
 			
 			Column lmY = cQ.addColumn("LocMinY");
 			lmY.setDataType(Column.DataType.DOUBLE);
 			
 			Column lmZ = cQ.addColumn("LocMinZ");
 			lmZ.setDataType(Column.DataType.DOUBLE);
 			
 			Column lMX = cQ.addColumn("LocMaxX");
 			lMX.setDataType(Column.DataType.DOUBLE);
 			
 			Column lMY = cQ.addColumn("LocMaxY");
 			lMY.setDataType(Column.DataType.DOUBLE);
 			
 			Column lMZ = cQ.addColumn("LocMaxZ");
 			lMZ.setDataType(Column.DataType.DOUBLE);
 			
 			cQ.exec();
 			
 			GiantShop.log.log(Level.INFO, "Shops table successfully created!");
 		}
 		
 		if(!this.dbDriver.tableExists("#__items")) {
 			ArrayList<String> field = new ArrayList<String>();
 			field.add("tablename");
 			field.add("version");
 			
 			InsertQuery iQ = this.dbDriver.insert("#__versions");
 			iQ.addFields(field);
 			iQ.addRow();
 			iQ.assignValue("tablename", "items");
 			iQ.assignValue("version", "1.0", InsertQuery.ValueType.RAW);
 			iQ.exec();
 			
 			CreateQuery cQ = this.dbDriver.create("#__items");
 			Column id = cQ.addColumn("id");
 			id.setDataType(Column.DataType.INT);
 			id.setLength(3);
 			id.setAutoIncr();
 			id.setPrimaryKey();
 			
 			Column iID = cQ.addColumn("itemID");
 			iID.setDataType(Column.DataType.INT);
 			iID.setLength(3);
 			
 			Column t = cQ.addColumn("type");
 			t.setDataType(Column.DataType.INT);
 			t.setLength(3);
 			t.setRawDefault("-1");
 			
 			Column sF = cQ.addColumn("sellFor");
 			sF.setDataType(Column.DataType.DOUBLE);
 			sF.setRawDefault("-1");
 			
 			Column bF = cQ.addColumn("buyFor");
 			bF.setDataType(Column.DataType.DOUBLE);
 			bF.setRawDefault("-1");
 			
 			Column s = cQ.addColumn("stock");
 			s.setDataType(Column.DataType.INT);
 			s.setLength(3);
 			s.setRawDefault("-1");
 			
 			Column pS = cQ.addColumn("perStack");
 			pS.setDataType(Column.DataType.INT);
 			pS.setLength(3);
 			pS.setRawDefault("1");
 			
 			Column sh = cQ.addColumn("shops");
 			sh.setDataType(Column.DataType.VARCHAR);
 			sh.setLength(100);
 			sh.setNull();
 			
 			cQ.exec();
 			
 			
 			GiantShop.log.log(Level.INFO, "Items table successfully created!");
 		}
 		
 		if(!this.dbDriver.tableExists("#__discounts")) {
 			ArrayList<String> field = new ArrayList<String>();
 			field.add("tablename");
 			field.add("version");
 			
 			InsertQuery iQ = this.dbDriver.insert("#__versions");
 			iQ.addFields(field);
 			iQ.addRow();
 			iQ.assignValue("tablename", "discounts");
 			iQ.assignValue("version", "1.2", InsertQuery.ValueType.RAW);
 			iQ.exec();
 			
 			CreateQuery cQ = this.dbDriver.create("#__discounts");
 			Column id = cQ.addColumn("id");
 			id.setDataType(Column.DataType.INT);
 			id.setLength(3);
 			id.setAutoIncr();
 			id.setPrimaryKey();
 			
 			Column iID = cQ.addColumn("itemID");
 			iID.setDataType(Column.DataType.INT);
 			iID.setLength(3);
 			
 			Column t = cQ.addColumn("type");
 			t.setDataType(Column.DataType.INT);
 			t.setLength(3);
 			t.setRawDefault("-1");
 			
 			Column d = cQ.addColumn("discount");
 			d.setDataType(Column.DataType.INT);
 			d.setLength(3);
 			d.setRawDefault("10");
 			
 			Column u = cQ.addColumn("user");
 			u.setDataType(Column.DataType.VARCHAR);
 			u.setLength(100);
 			u.setNull();
 			
			Column grp = cQ.addColumn("grp");
 			grp.setDataType(Column.DataType.VARCHAR);
 			grp.setLength(100);
 			grp.setNull();
 			
 			cQ.exec();
 			
 			GiantShop.log.log(Level.INFO, "Discounts type table successfully created!");
 		}
 	}
 	
 	private void checkUpdate() {
 		QueryResult QRes = this.dbDriver.select("tablename", "version").from("#__versions").exec();
 		
 		QueryResult.QueryRow QR;
 		while(null != (QR = QRes.getRow())) {
 			String table = QR.getString("tablename");
 			Double version = QR.getDouble("version");
 			
 			if(table.equalsIgnoreCase("shops") && version < curS) {
 				Shops.run(version);
 			}else if(table.equalsIgnoreCase("items") && version < curI) {
 				Items.run(version);
 			}else if(table.equalsIgnoreCase("discounts") && version < curD) {
 				Discounts.run(version);
 			}else if(table.equalsIgnoreCase("log") && version < curL) {
 				Logs.run(version);
 			}	
 		}
 	}
 	
 	public dbInit(GiantShop plugin) {
 		this.dbDriver = plugin.getDB().getEngine();
 
 		this.init();
 		this.checkUpdate();
 	}
 	
 }
