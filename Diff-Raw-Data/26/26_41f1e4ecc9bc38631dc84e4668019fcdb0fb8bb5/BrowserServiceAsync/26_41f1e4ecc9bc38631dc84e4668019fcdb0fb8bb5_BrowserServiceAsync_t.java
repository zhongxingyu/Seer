 /*--------------------------------------------------------------------------
  *  Copyright 2007 utgenome.org
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
 // GenomeBrowser Project
 //
 // BrowserServiceAsync.java
 // Since: Apr 20, 2007
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client;
 
 import java.util.List;
 
 import org.utgenome.gwt.utgb.client.bean.DatabaseEntry;
 import org.utgenome.gwt.utgb.client.bean.track.TrackDescription;
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.ChrRange;
 import org.utgenome.gwt.utgb.client.bio.CompactWIGData;
 import org.utgenome.gwt.utgb.client.bio.GenomeDB;
 import org.utgenome.gwt.utgb.client.bio.KeywordSearchResult;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.utgenome.gwt.utgb.client.bio.ReadQueryConfig;
 import org.utgenome.gwt.utgb.client.bio.SAMRead;
 import org.utgenome.gwt.utgb.client.bio.WigGraphData;
 import org.utgenome.gwt.utgb.client.track.bean.TrackBean;
 import org.utgenome.gwt.utgb.client.view.TrackView;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
 
public interface BrowserServiceAsync extends RemoteService {
 
 	public void createTrackView(String silk, AsyncCallback<TrackView> callback);
 
 	public void getTrackView(String viewName, AsyncCallback<TrackView> callback);
 
 	public void getHTTPContent(String url, AsyncCallback<String> callback);
 
 	public void getTrackDescription(String url, AsyncCallback<TrackDescription> callback);
 
 	public void getDatabaseCatalog(String jdbcAddress, AsyncCallback<String> callback);
 
 	public void getTableData(String jdbcAddress, String tableName, AsyncCallback<String> callback);
 
 	public void getTrackList(int entriesPerPage, int page, AsyncCallback<List<TrackBean>> callback);
 
 	public void numHitsOfTracks(String prefix, AsyncCallback<Integer> callback);
 
 	public void getTrackList(String prefix, int entriesPerPage, int page, AsyncCallback<List<TrackBean>> callback);
 
 	public void keywordSearch(String species, String revision, String keyword, int entriesPerPage, int page, AsyncCallback<KeywordSearchResult> callback);
 
 	public void getChrRegion(String species, String revision, AsyncCallback<ChrRange> callback);
 
 	public void getChildDBGroups(String parentDBGroup, AsyncCallback<List<String>> callback);
 
 	public void getDBNames(String dbGroup, AsyncCallback<List<String>> callback);
 
 	public void getDBEntry(String dbGroup, AsyncCallback<List<DatabaseEntry>> callback);
 
 	public void getWigDataList(String fileName, int windowWidth, ChrLoc location, AsyncCallback<List<WigGraphData>> callback);
 
 	public void getCompactWigDataList(String fileName, int windowWidth, ChrLoc location, AsyncCallback<List<CompactWIGData>> callback);
 
 	public void getSAMReadList(String readFileName, String refSeqFileName, AsyncCallback<List<SAMRead>> callback);
 
 	public void querySAMReadList(String bamFileName, String indexFileName, String refSeqFileName, String rname, int start, int end,
 			AsyncCallback<List<SAMRead>> callback);
 
 	public void getRefSeq(String refSeqFileName, String rname, int start, int end, AsyncCallback<String> callback);
 
 	public void getOnGenomeData(GenomeDB db, ChrLoc range, ReadQueryConfig config, AsyncCallback<List<OnGenome>> callback);
 
 }
