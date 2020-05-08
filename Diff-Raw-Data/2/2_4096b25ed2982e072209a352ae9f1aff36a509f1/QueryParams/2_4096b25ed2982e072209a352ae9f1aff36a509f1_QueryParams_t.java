 package com.wiley.frommers.digester.query;
 
 /**
  * Query parameters used to call the feeds.
  */
 public enum QueryParams {
 
     PAGE("page"),
     N_PER_PAGE("nPerPage"),
     QUERY("query"),
     TYPE("type"),
     TYPE_ID("typeId"),
     TYPE_CD("typeCd"),
     AUTO_HIDE("autoHide"),
     DAYS_AHEAD("daysAhead"),
     RANK_ID("rankId"),
     MIN_RANK_ID("minRankId"),
     MAX_RANK_ID("maxRankId"),
     LONGITUDE("longitude"),
     LATITUDE("latitude"),
     MILES("miles"),
    SUB_TYPE("subType"),
     SHOW_DEPTH("showDepth"),
     SHOW_MAX("showMax"),
     AUDIENCE_INTEREST_ID("audienceInterestId"),
     GUIDE_STRUCTURE_ID("guideStructureId"),
     GUIDE_STRUCTURE_TYPE_ID("guideStructureTypeId"),
     GUIDE_ID("guideId"),
     IS_GUIDE("isGuide"),
     ITEM_OF_INTEREST_ID("itemOfInterestId"),
     MEDIA_ID("mediaId"),
     HAS_GUIDE("hasGuide"),
     PARENT_ID("parentId"),
     SLIDESHOW_ID("slideshowId"),
     LOCATION_ID("locationId"),
     LOC_QUERY("locQuery"),
     MAPPED_LOCATION_TYPE("mappedLocationType"),
     MAPPED_LOCATION_ID("mappedLocationId"),
     SHOW_COUNT("showCount"),
     SHOW_CHILDREN("showChildren"),
     DEPTH("depth"),
     START_DATE("startDate"),
     END_DATE("endDate"),
     FROMMERS_DESTINATION_ID("frommersDestinationId");
     
     private String name;
     
     private QueryParams(String name) {
         this.name = name;
     }
     
     public String getName() {
         return this.name;
     }
 }
