 package com.malcom.library.android.module.campaign;
 
 import com.malcom.library.android.utils.JSONHelper;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.*;
 
 /**
  * MCMCampaignDTO.
  * Class to store information about a campaign.
  */
 public class MCMCampaignDTO {
     public static enum CampaignType {IN_APP_PROMOTION, IN_APP_RATE_MY_APP, IN_APP_CROSS_SELLING};
 
     public static enum CampaignPosition {BOTTOM, TOP, FULL_SCREEN, MIDDLE_PORTRAIT, MIDDLE_LANDSCAPE};
 
     //JSON tags
     private static final String ATTR_CAMPAIGN_ID = "id";
     private static final String ATTR_NAME = "name";
     private static final String ATTR_START = "start";
     private static final String ATTR_END = "end";
     private static final String ATTR_CREATED_ON = "createdOn";
     private static final String ATTR_DESCRIPTION_FEATURE = "descriptionFeature";
     private static final String ATTR_PROMOTION_DESCRIPTION = "promotionDescription";
     private static final String ATTR_TYPE = "type";
     private static final String ATTR_MEDIA_FEATURE = "mediaFeature";
     private static final String ATTR_MEDIA = "media";
     private static final String ATTR_CAMPAIGN_POSITION = "position";
     private static final String ATTR_PROMOTION_FEATURE = "promotionFeature";
     private static final String ATTR_PROMOTION_TYPE = "promotionType";
     private static final String ATTR_PROMOTION_IDENTIFIER = "promotionIdentifier";
     private static final String ATTR_CLIENT_LIMIT_FEATURE = "clientLimitFeature";
     private static final String ATTR_CLIENT_LIMIT_TYPE = "clientLimitType";
     private static final String ATTR_LIMIT_VALUE = "limitValue";
     private static final String ATTR_CUSTOM_PARAMS = "customParamsFeature";
     private static final String ATTR_CUSTOM_PARAMS_PROPERTIES = "properties";
     private static final String ATTR_WEIGHT_CAMPAIGN = "weight";
 
     //Attributes
     private String campaignId;
     private String name;
     private String start;
     private String end;
     private String createdOn;
     private String campaignDescription;
     private CampaignType type;
     private String media;
     private CampaignPosition campaignPosition;
     private String promotionType;
     private String promotionIdentifier;
     private String clientLimitType;
     private String limitValue;
     private Map<String, Object> customParams;
     private int weight;
 
     public MCMCampaignDTO(JSONObject json) {
 
         String typeJSON = null;
 
         try {
             campaignId = json.getString(ATTR_CAMPAIGN_ID);
             name = json.getString(ATTR_NAME);
             start = json.getString(ATTR_START);
             end = json.getString(ATTR_END);
             createdOn = json.getString(ATTR_CREATED_ON);
             if (json.has(ATTR_DESCRIPTION_FEATURE))
                campaignDescription = json.getJSONObject(ATTR_DESCRIPTION_FEATURE).getString(ATTR_PROMOTION_DESCRIPTION);
             typeJSON = json.getString(ATTR_TYPE);
             if (json.has(ATTR_MEDIA_FEATURE))
                 hydrateMediaFeature(json.getJSONObject(ATTR_MEDIA_FEATURE));
             if (json.has(ATTR_PROMOTION_FEATURE))
                 hydratePromotionFeature(json.getJSONObject(ATTR_PROMOTION_FEATURE));
             if (json.has(ATTR_CLIENT_LIMIT_FEATURE))
                 hydrateClientLimitFeature(json.getJSONObject(ATTR_CLIENT_LIMIT_FEATURE));
             if (json.has(ATTR_CUSTOM_PARAMS) && json.getJSONObject(ATTR_CUSTOM_PARAMS).has(ATTR_CUSTOM_PARAMS_PROPERTIES))
                 customParams = JSONHelper.toMap(json.getJSONObject(ATTR_CUSTOM_PARAMS).getJSONObject(ATTR_CUSTOM_PARAMS_PROPERTIES));
             weight = json.has(ATTR_WEIGHT_CAMPAIGN) ? json.getInt(ATTR_WEIGHT_CAMPAIGN) : 1;
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         if (typeJSON != null) {
 
             if (typeJSON.equalsIgnoreCase(CampaignType.IN_APP_PROMOTION.name())) {
                 type = CampaignType.IN_APP_PROMOTION;
             } else if (typeJSON.equalsIgnoreCase(CampaignType.IN_APP_RATE_MY_APP.name())) {
                 type = CampaignType.IN_APP_RATE_MY_APP;
             } else if (typeJSON.equalsIgnoreCase(CampaignType.IN_APP_CROSS_SELLING.name())) {
                 type = CampaignType.IN_APP_CROSS_SELLING;
             } else {
                 type = CampaignType.IN_APP_PROMOTION;
             }
         }
     }
 
     private void hydrateMediaFeature(JSONObject json) {
 
         String position = null;
         try {
             if (json.has(ATTR_MEDIA))
                 media = (String) json.get(ATTR_MEDIA);
             if (json.has(ATTR_CAMPAIGN_POSITION))
                 position = (String) json.getString(ATTR_CAMPAIGN_POSITION);
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         if (position != null) {
 
             if (position.equals(CampaignPosition.BOTTOM.name())) {
                 campaignPosition = CampaignPosition.BOTTOM;
             } else if (position.equals(CampaignPosition.TOP.name())) {
                 campaignPosition = CampaignPosition.TOP;
             } else if (position.equals(CampaignPosition.FULL_SCREEN.name())) {
                 campaignPosition = CampaignPosition.FULL_SCREEN;
             } else if (position.equals(CampaignPosition.MIDDLE_PORTRAIT.name())) {
                 campaignPosition = CampaignPosition.MIDDLE_PORTRAIT;
             } else if (position.equals(CampaignPosition.MIDDLE_LANDSCAPE.name())) {
                 campaignPosition = CampaignPosition.MIDDLE_LANDSCAPE;
             } else {
                 //if there is no position, set Bottom by default
                 campaignPosition = CampaignPosition.BOTTOM;
             }
         } else {
             //if there is no position, set Bottom by default
             campaignPosition = CampaignPosition.BOTTOM;
         }
     }
 
     private void hydratePromotionFeature(JSONObject json) {
         try {
             promotionType = (String) json.get(ATTR_PROMOTION_TYPE);
             promotionIdentifier = (String) json.getString(ATTR_PROMOTION_IDENTIFIER);
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
 
     private void hydrateClientLimitFeature(JSONObject json) {
         try {
             clientLimitType = (String) json.get(ATTR_CLIENT_LIMIT_TYPE);
             limitValue = (String) json.getString(ATTR_LIMIT_VALUE);
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
 
     public String getCampaignId() {
         return campaignId;
     }
 
     public String getName() {
         return name;
     }
 
     public String getStart() {
         return start;
     }
 
     public String getEnd() {
         return end;
     }
 
     public String getCreatedOn() {
         return createdOn;
     }
 
     public String getCampaignDescription() {
         return campaignDescription;
     }
 
     public CampaignType getType() {
         return type;
     }
 
     public String getMedia() {
         return media;
     }
 
     public CampaignPosition getCampaignPosition() {
         return campaignPosition;
     }
 
     public void setCampaignPosition(CampaignPosition campaignPosition) {
         this.campaignPosition = campaignPosition;
     }
 
     public String getPromotionType() {
         return promotionType;
     }
 
     public String getPromotionIdentifier() {
         return promotionIdentifier;
     }
 
     public String getClientLimitType() {
         return clientLimitType;
     }
 
     public String getLimitValue() {
         return limitValue;
     }
 
     public Object getCustomParam(String key) {
         return customParams.get(key);
     }
 
     public int getWeight() {
         return weight;
     }
 
     public boolean isFullScreen() {
        return (getCampaignPosition() != CampaignPosition.BOTTOM &&
                 getCampaignPosition() != CampaignPosition.TOP);
     }
 }
