 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.type;
 
 import javax.xml.bind.annotation.XmlType;
 
 @XmlType (namespace="http://www.orangeleap.com/orangeleap/schemas")
 public enum PageType {
     constituent("/constituent.htm"),
     constituentSearch("/constituentSearch.htm"),
     constituentSearchResults("/constituentSearchResults.htm"),
     gift("/gift.htm"),
     giftList("/giftList.htm"),
     giftSearch("/giftSearch.htm"),
     giftSearchResults("/giftSearchResults.htm"),
 	giftCombinedDistributionLines("/giftCombinedDistributionLines.htm"),
     giftView("/giftView.htm"),
     adjustedGift("/giftAdjustment.htm"),
     adjustedGiftView("/giftAdjustmentView.htm"),
     paymentManager("/paymentManager.htm"),
     paymentManagerEdit("/paymentManagerEdit.htm"),
    paymentSourceList("/paymentSourceList.htm"),
     addressSource("/addressSource.htm"),
     queryLookup("/queryLookup.htm"),
     addressManager("/addressManager.htm"),
     addressManagerEdit("/addressManagerEdit.htm"),
     addressList("/addressList.htm"),
     recurringGift("/recurringGift.htm"),
     recurringGiftList("/recurringGiftList.htm"),
     recurringGiftSearch("/recurringGiftSearch.htm"),
     recurringGiftSearchResults("/recurringGiftSearchResults.htm"),
     recurringGiftView("/recurringGiftView.htm"),
     picklistItems("/picklistItems.htm"),
     pledge("/pledge.htm"),
     pledgeList("/pledgeList.htm"),
     pledgeSearch("/pledgeSearch.htm"),
     pledgeSearchResults("/pledgeSearchResults.htm"),
     pledgeView("/pledgeView.htm"),
     membership("/membership.htm"),
     membershipList("/membershipList.htm"),
     membershipSearch("/membershipSearch.htm"),
     membershipSearchResults("/membershipSearchResults.htm"),
     membershipView("/membershipView.htm"),
     giftInKind("/giftInKind.htm"),
     giftInKindList("/giftInKindList.htm"),
     emailManager("/emailManager.htm"),
     emailManagerEdit("/emailManagerEdit.htm"),
    emailList("/emailList.htm"),
     phoneManager("/phoneManager.htm"),
     phoneManagerEdit("/phoneManagerEdit.htm"),
    phoneList("/phoneList.htm"),
     importexport("/importexport.htm"),
     paymentHistory("/paymentHistory.htm"),
     communicationHistory("/communicationHistory.htm"),
     communicationHistoryList("/communicationHistoryList.htm"),
     communicationHistorySearch("/communicationHistorySearch.htm"),
     communicationHistorySearchResults("/communicationHistorySearchResults.htm"),
     communicationHistoryView("/communicationHistoryView.htm"),
     constituentcustomfieldrelationship("/relationshipCustomize.htm"),
     fieldRelationshipCustomize("/fieldRelationshipCustomize.htm"),
     audit("/audit.htm"),
     siteAudit("/siteAudit.htm"),
     customField("/customField.htm"),
     siteSettings("/siteSettings.htm"),
     postbatch("/postbatch.htm"),
    logView("/logView.htm");
 
     private String pageName;
 
     private PageType(String pageName) {
         this.pageName = pageName;
     }
 
     public String getName() {
         return this.name();
     }
 
     public String getPageName() {
         return pageName;
     }
 
 	public static PageType findByUrl(String url) {
 		for (PageType type : PageType.values()) {
 			if (type.getPageName().equalsIgnoreCase(url)) {
 				return type;
 			}
 		}
 		return null;
 	}
 }
