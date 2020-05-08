 package org.netvogue.server.neo4japi.repository;
 
 import org.netvogue.server.neo4japi.common.NetworkStatus;
 import org.netvogue.server.neo4japi.domain.*;
 import org.netvogue.server.neo4japi.service.CollectionData;
 import org.netvogue.server.neo4japi.service.LinesheetData;
 import org.netvogue.server.neo4japi.service.ReferenceData;
 import org.netvogue.server.neo4japi.service.StylesheetData;
 import org.netvogue.server.neo4japi.service.UserData;
 import org.springframework.data.neo4j.annotation.Query;
 import org.springframework.data.neo4j.repository.GraphRepository;
 
 public interface UserRepository extends GraphRepository<User> {
 	User findByemail(String email);
 	User findByusername(String username);
 	
 	@Query( "START n=node:email({0}) WHERE n.id <> {1} RETURN n")
 	User findByemailAndId(String email, Long id);
 	//User findByemailOrUsername(String email, String username);
 	
 	@Query( "START n=node:search(username={0}) " +
 			"MATCH n-[?:users_carried]-uc, n-[?:has_category]-pl " +
			"RETURN n as user, collect(pl.productline) as productlines, " +
 			"collect(uc.name) as brandnames, collect(uc.username) as brandusernames")
 	UserData getUserByusername(String username);
 	
 	@Query( "START n=node:search({0}) " +
 			"WHERE has(n.userType)" +
 			"RETURN n")
 	Iterable<User> doBasicSearch(String query);
 	
 	/*@Query( "start category=node:productline(productline={cat}) " +
 			"with collect(category) as categories " +
 			"start user=node:search({search}) " +
 			"where p<>user AND ALL( c in categories WHERE user-[:HAS_CAT]->c)  " +
 			"return user " +
 			"skip {pagenumber*pagesize} limit{pagesize}")
 	Iterable<User> doAdvancedSearch(@Param("cat") List<String> Categories, 
 									@Param("search") Map<String, String> searchIndex,
 									@Param("pagenumber") long pagenumber, @Param("pagesize") long pagesize);*/
 	
 	/*@Query( "START category=node:productline({0}) " +
 			"WITH collect(category) as categories " +
 			"START user=node:search({1})  " +
 			"WHERE ALL( c in categories WHERE user-[:has_category]->c) AND " +
 			"user.userType! = {2} " +
 			"WITH user START userscarried=node:search({3}) " +
 			"MATCH user-[:users_carried]-userscarried " +
 			"RETURN user")*/
 	@Query( "START category=node:productline({0}),  usercarried=node:search({3}) " +
 			"WITH collect(category) as categories, collect(usercarried) as userscarried " +
 			"START user=node:search({1})  " +
 			"WHERE has(user.userType) AND " +
 			"ALL( c in categories WHERE user-[:has_category]->c) AND " +
 			"ALL(u in userscarried WHERE user-[:users_carried]-u) " +
 			//"user.userType! = {2} " +
 			"RETURN user")
 	Iterable<User> doAdvancedSearch(String Categories, String search, String usertype, String userscarried);
 	
 	@Query( "START n=node:search(username={0}) MATCH n-[r:NETWORK]-f WHERE f.username={1} " +
 			"RETURN r.status")
 	NetworkStatus getNetworkStatus(String username1, String username2);
 	
 	//Related to references
 	@Query(	"START n=node:search(username={0}) MATCH n-[rels:NETWORK*2.2]-references " +
 			"WHERE ALL(r in rels WHERE r.status? = 'CONFIRMED') and not(n-[:NETWORK]-references) " +
 			"WITH n, references " +
 			"MATCH mutualfriends = n-[f1?:NETWORK]-(mf)-[f2?:NETWORK]-references " +
 			"WHERE f1.status = 'CONFIRMED' and f2.status = 'CONFIRMED' " +
 			"RETURN references, count(distinct mutualfriends) as mutualfriends " +
 			"ORDER BY count(mutualfriends) DESC " +
 			"SKIP 0 LIMIT 2")
 	Iterable<ReferenceData> getReferences(String username, int pagenumber, int resultsperpage);
 	
 	//Queries related to gallery
 	@Query( "START n=node:search(username={0}) MATCH n-[:GALLERY]->g RETURN g ORDER BY g.createdDate DESC")
 	Iterable<Gallery> getGalleries(String username);
 	
 	@Query( "START n=node:search(username={0}) MATCH n-[:GALLERY]->g WHERE g.galleryname =~ {1} " +
 			"RETURN g ORDER BY g.createdDate DESC")
 	Iterable<Gallery> searchGalleryByName(String username, String galleryname);
 	
 	//Queries related to Print campaigns
 	@Query( "START n=node:search(username={0}) MATCH n-[:PRINTCAMPAIGN]->pc RETURN pc ORDER BY pc.createdDate DESC")
 	Iterable<PrintCampaign> getPrintCampaigns(String username);
 	
 	@Query( "START n=node:search(username={0}) MATCH n-[:PRINTCAMPAIGN]->pc WHERE pc.printcampaignname =~ {1} " +
 			"RETURN pc ORDER BY pc.createdDate DESC")
 	Iterable<PrintCampaign> searchPrintCampaignByName(String username, String printcampaignname);
 	
 	//Queries related to editorials
 	@Query( "START n=node:search(username={0}) MATCH n-[:EDITORIAL]->e RETURN e ORDER BY e.createdDate DESC")
 	Iterable<Editorial> getEditorials(String username);
 	
 	@Query( "START n=node:search(username={0}) MATCH n-[:EDITORIAL]->e WHERE e.editorialname =~ {1} " +
 			"RETURN e ORDER BY e.createdDate DESC")
 	Iterable<Editorial> searchEditorialByName(String username, String editorialname);
 	
 	//queries related to collections
 	@Query( "START n=node:search(username={0}) MATCH n-[:COLLECTION]->collection " +
 			"RETURN n.name as name, collection ORDER BY collection.createdDate DESC")
 	Iterable<CollectionData> getCollections(String username);
 	
 	@Query( "START n=node:search(username={0}) " +
 			"MATCH n-[r:NETWORK]-user WHERE r.status? = 'CONFIRMED' " +
 			"WITH user " +
 			"MATCH user-[:COLLECTION]->collection " +
 			"RETURN user.name as name, collection ORDER BY collection.createdDate DESC")
 	Iterable<CollectionData> getMyNetworkCollections(String username);
 	
 	@Query( "START n=node:search(username={0}), categories = node:productline({2}) " +
 			"MATCH n-[:COLLECTION]->collection-[:Collection_Category]-categories " +
 			"WHERE collection.collectionseasonname =~ {1} " +
 			"RETURN DISTINCT n.name as name, collection ORDER BY collection.createdDate DESC")
 	Iterable<CollectionData> searchCollections(String username, String seasonname, String category);
 	
 	@Query("START n=node:search(username={0}) " +
 			"MATCH n-[r:NETWORK]-user WHERE r.status? = 'CONFIRMED' AND user.name = {4} " +
 			"WITH user START categories = node:productline({2}) " +
 			"MATCH user-[:COLLECTION]->collection-[:Collection_Category]-categories " +
 			"WHERE collection.collectionseasonname =~ {1} " +
 			"RETURN DISTINCT user.name as name, collection ORDER BY collection.createdDate DESC")
 	Iterable<CollectionData> searchMyNetworkCollections(String username, String seasonname, String category, String brandname);
 
 	//queries related to stylesheets
 	@Query( "START n=node:search(username={0}) MATCH n-[:STYLESHEET]->stylesheet " +
 			"RETURN n.name as name, stylesheet ORDER BY stylesheet.createdDate DESC")
 	Iterable<StylesheetData> getStylesheets(String username);
 	
 	@Query( "START n=node:search(username={0}), categories = node:productline({2}) " +
 			"MATCH n-[:STYLESHEET]->stylesheet-[:Stylesheet_Category]-categories " +
 			"WHERE stylesheet.stylesheetname =~ {1} " +
 			"RETURN DISTINCT n.name as name, stylesheet ORDER BY stylesheet.createdDate DESC")
 	Iterable<StylesheetData> searchStylesheets(String username, String stylesheetname, String category);
 	
 	//queries related to linesheets
 	@Query( "START n=node:search(username={0}) MATCH n-[:LINESHEET]->linesheet " +
 			"RETURN n.name as name, linesheet ORDER BY linesheet.createdDate DESC")
 	Iterable<LinesheetData> getLinesheets(String username);
 	
 	@Query( "START n=node:search(username={0}) " +
 			"MATCH n-[r:NETWORK]-user WHERE r.status? = 'CONFIRMED' " +
 			"WITH user " +
 			"MATCH user-[:LINESHEET]->linesheet " +
 			"RETURN user.name as name, linesheet ORDER BY linesheet.createdDate DESC")
 	Iterable<LinesheetData> getMyNetworkLinesheets(String username);
 	
 	@Query( "START n=node:search(username={0}), categories = node:productline({2}) " +
 			"MATCH n-[:LINESHEET]->linesheet-[:Linesheet_Category]-categories " +
 			"WHERE linesheet.linesheetname =~ {1} " +
 			"AND linesheet.deliveryDate >= {3} AND linesheet.deliveryDate <= {4} " +
 			"WITH n,linesheet MATCH linesheet-[?:LS_STYLE]-style " +
 			"WHERE (style=null AND 0 = {5} AND 0 = {6}) OR " +
 			"(style.price >= {5} AND style.price <= {6}) " +
 			"RETURN n.name as name, linesheet ORDER BY linesheet.createdDate DESC")
 	Iterable<LinesheetData> searchLinesheets(String username, String linesheetname, String category,
 											String fromdate, String todate,
 											long fromPrice, long toPrice);
 	
 	@Query( "START n=node:search(username={0}) " +
 			"MATCH n-[r:NETWORK]-user WHERE r.status? = 'CONFIRMED' AND user.name = {7} " +
 			"WITH user START categories = node:productline({2}) " +
 			"MATCH user-[:LINESHEET]->linesheet-[:Linesheet_Category]-categories " +
 			"WHERE linesheet.linesheetname =~ {1} " +
 			"AND linesheet.deliveryDate >= {3} AND linesheet.deliveryDate <= {4} " +
 			"WITH user,linesheet MATCH linesheet-[?:LS_STYLE]-style " +
 			"WHERE (style=null AND 0 = {5} AND 0 = {6}) OR " +
 			"(style.price >= {5} AND style.price <= {6}) " +
 			"RETURN user.name as name, linesheet ORDER BY linesheet.createdDate DESC")
 	Iterable<LinesheetData> searchMyNetworkLinesheets(String username, String linesheetname, String category,
 														String fromdate, String todate,
 														long fromPrice, long toPrice,
 														String brandname);
 	
 
 }
