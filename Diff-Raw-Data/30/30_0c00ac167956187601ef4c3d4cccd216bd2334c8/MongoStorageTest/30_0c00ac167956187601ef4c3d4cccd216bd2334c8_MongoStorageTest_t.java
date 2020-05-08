 /**
  * 	Copyright (C) 2011 Sam Macbeth <sm1106 [at] imperial [dot] ac [dot] uk>
  *
  * 	This file is part of Presage2.
  *
  *     Presage2 is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Presage2 is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser Public License for more details.
  *
  *     You should have received a copy of the GNU Lesser Public License
  *     along with Presage2.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.imperial.presage2.db.mongodb;
 
 import uk.ac.imperial.presage2.core.db.GenericStorageServiceTest;
 
 public class MongoStorageTest extends GenericStorageServiceTest {
 
 	MongoStorage mongo;
 
 	@Override
 	public void getDatabase() {
		mongo = new MongoStorage("ee-sm1106.ee.ic.ac.uk", "presage-test");
 		// clear db
 		try {
 			mongo.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		mongo.db.dropDatabase();
 		mongo.db = mongo.mongo.getDB(mongo.dbName);
 
 		this.db = mongo;
 		this.sto = mongo;
 	}
 
 }
