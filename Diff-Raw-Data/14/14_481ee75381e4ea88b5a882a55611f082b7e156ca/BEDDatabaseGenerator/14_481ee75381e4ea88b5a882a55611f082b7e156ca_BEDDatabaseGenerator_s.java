 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
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
 // utgb-shell Project
 //
 // BEDDatabaseGenerator.java
 // Since: May 26, 2009
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.format.bed;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Reader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.utgenome.gwt.utgb.client.bio.Gene;
 import org.utgenome.gwt.utgb.server.app.BEDViewer.BEDTrack;
 import org.xerial.core.XerialException;
 import org.xerial.lens.Lens;
 import org.xerial.util.log.Logger;
 
 public class BEDDatabaseGenerator {
 
 	private static Logger _logger = Logger.getLogger(BEDDatabaseGenerator.class);
 
 	public static void toSQLiteDB(Reader bedInput, String dbName) throws IOException, XerialException {
 		BED2SilkReader reader = new BED2SilkReader(bedInput);
 
 		DBBuilder query;
 		try {
 
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
 
 			query = new DBBuilder(conn);
 			Lens.loadSilk(query, reader);
 
 			query.dispose();
 		}
 		catch (Exception e) {
 			_logger.error(e);
 		}
 	}
 
 	public static class DBBuilder {
 
 		Connection conn;
 		PreparedStatement p1;
 		PreparedStatement p2;
 		Statement stat;
 
 		public DBBuilder(Connection conn) throws SQLException {
 			this.conn = conn;
 			this.stat = conn.createStatement();
 
 			stat.executeUpdate("pragma synchronous = off");
 
 			conn.setAutoCommit(false);
 
 			stat.executeUpdate("drop table if exists track");
 			stat.executeUpdate("drop table if exists gene");
 			stat.executeUpdate("create table track (object blob)");
 
 			stat.executeUpdate("create table gene (coordinate text, start integer, end integer, " + "name text, score integer, strand text, cds text, "
 					+ "exon text, color text)");
 
			stat.executeUpdate("create index gene_index on gene (coordinate, start)");

 			p1 = conn.prepareStatement("insert into track values(?)");

 			p2 = conn.prepareStatement("insert into gene values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
 
 		}
 
 		public void dispose() throws SQLException {
 
 			conn.commit();
 
 			p1.close();
 			p2.close();
 			stat.close();
 			conn.close();
 		}
 
 		public void addTrack(BEDTrack track) {
 			// store track data to db
 			try {
 				ByteArrayOutputStream buf = new ByteArrayOutputStream();
 				ObjectOutputStream out = new ObjectOutputStream(buf);
 				out.writeObject(track);
 				out.flush();
 
 				p1.setBytes(1, buf.toByteArray());
 				p1.execute();
 			}
 			catch (Exception e) {
 				_logger.error(e);
 			}
 		}
 
 		public void addGene(Gene gene) {
 			// store gene data to db
 			try {
 				p2.setString(1, gene.getChr());
 				p2.setLong(2, gene.getStart());
 				p2.setLong(3, gene.getEnd());
 				p2.setString(4, gene.getName());
 				p2.setInt(5, gene.getScore());
 				p2.setString(6, gene.getStrand());
 				p2.setString(7, gene.getCDS().toString());
 				p2.setString(8, gene.getExon().toString());
 				p2.setString(9, gene.getColor());
 
 				p2.execute();
 			}
 			catch (Exception e) {
 				_logger.error(e);
 			}
 		}
 	}
 }
