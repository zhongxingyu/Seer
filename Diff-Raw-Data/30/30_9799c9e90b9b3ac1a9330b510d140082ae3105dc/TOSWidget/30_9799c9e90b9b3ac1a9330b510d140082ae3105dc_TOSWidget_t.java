 package project.client.tos;
 
 import project.client.login.LoginInfo;
 import project.client.login.LoginWidget;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class TOSWidget extends VerticalPanel{
 	public TOSWidget(final LoginInfo loginInfo){
 		System.out.println("tos constructor start");
 	//ScrollPanel panel = new ScrollPanel();
 	TextArea area = new TextArea();
 	DOM.setStyleAttribute(area.getElement(), "border", "1px");  //removes border
 	DOM.setStyleAttribute(area.getElement(), "width", "1150px");  //fixes size error on firefox
 	DOM.setStyleAttribute(area.getElement(), "height", "768px");
 	area.setSize("1150px", "768px");
 	add(area);
 	System.out.println("area added, text not set");
 	String text =
 			"Terms and Conditions of Service"+
 			"1. Website and Service Terms and Conditions - General.\n\n"+
 
 			"The CrowdCoding website (\"Website\") and related services (together with the Website,"+
 			"the \"Service\") are operated by a collaboration of researchers at Carnegie Mellon"+
 			"University and/or University of California at Irvine. (\"Crowdcoding,\" \"us,\" or \"we\")."+
 			"Access and use of the Service is subject to the following Terms and Conditions of"+
 			"Service (\"Terms and Conditions\"). Accessing or using any part of the Service constitutes"+
 			"an agreement that you have read, understood, and agree to be bound by these Terms and"+
 			"Conditions. CrowdCoding may amend, update or change these Terms and Conditions."+
 			"If we do this, we will post a notice that we have made changes to these Terms and"+
 			"Conditions on the Website for at least 7 days after the changes are posted and will"+
 			"indicate at the bottom of the Terms and Conditions the date these terms were last revised."+
 			"Any revisions to these Terms and Conditions will become effective the earlier of (i)"+
 			"the end of such 7-day period or (ii) the first time you access or use the Service after"+
 			"such changes. If you do not agree to abide by these Terms and Conditions, you are not"+
 			"authorized to use, access or participate in the Service.\n\n"+
 
 			"2. Description of Website and Service\n\n"+
 
 			"The Service allows users to learn or practice software engineering and programming"+
 			"while they complete tasks that are small parts of larger projects. Users are presented"+
 			"with different types of activities; while they perform these activities, they also"+
 			"generate valuable data such as Web applications, operational programs, and designs."+
 			"CrowdCoding may, in its sole discretion and at any time update, change, suspend, make"+
 			"improvements to or discontinue any aspect of the Service, temporarily or permanently.\n\n"+
 
 			"3. Registration\n\n"+
 
 			"In connection with registering for and using the Service, you agree (i) to provide"+
 			"accurate, current and complete information about you and/or your organization as"+
 			"requested by CrowdCoding; (ii) to maintain the confidentiality of your password"+
 			"and other information related to the security of your account; (iii) to maintain and"+
 			"promptly update any registration information you provide to CrowdCoding, to keep such"+
 			"information accurate, current and complete; and (iv) to be fully responsible for all use of"+
 			"your account and for any actions that take place through your account.\n\n"+
 
 			"4. Your Representations and Warranties\n\n" +
 			
 			"You represent and warrant to CrowdCoding that your access and use of the Service will"+
 			"be in accordance with these Terms and Conditions and with all applicable laws, rules"+
 			"and regulations of the United States and any other relevant jurisdiction, including those"+
 			"regarding online conduct or acceptable content, and those regarding the transmission of"+
 			"data or information exported from the United States and/or the jurisdiction in which you"+
 			"reside.\n\n"+
 
 			"5. Inappropriate Use\n\n"+
 
 			"You will not upload, display or otherwise provide on or through the Service any content"+
 			"that: (i) is libelous, defamatory, abusive, threatening, harassing, hateful, offensive or"+
 			"otherwise violates any law or infringes upon the right of any third party (including"+
 			"copyright, trademark, privacy, publicity or other personal or proprietary rights); or (ii) in"+
 			"CrowdCoding's sole judgment, is objectionable or which restricts or inhibits any other"+
 			"person from using the Service or which may expose CrowdCoding or its users to any"+
 			"harm or liability of any time.\n\n"+
 
 			"6. Indemnification of CrowdCoding\n\n"+
 
 			"You agree to defend, indemnify and hold harmless CrowdCoding, Carnegie Mellon"+
 			"University, and the University of California, and their directors, officers, employees,"+
 			"contractors, agents, suppliers, licensors, successors and assigns, from and against any"+
 			"and all losses, claims, causes of action, obligations, liabilities and damages whatsoever,"+
 			"including attorneys' fees, arising out of or relating to your access or use of the Service or"+
 			"your breach of any of these Terms and Conditions.\n\n"+
 
 			"7. NO REPRESENTATIONS OR WARRANTIES\n\n"+
 
 			"THE SERVICE, INCLUDING ALL IMAGES, AUDIO FILES AND OTHER"+
 			"CONTENT THEREIN, AND ANY OTHER INFORMATION, PROPERTY AND"+
 			"RIGHTS GRANTED OR PROVIDED TO YOU BY CROWDCODING ARE"+
 			"PROVIDED TO YOU ON AN \"AS IS\" BASIS. CROWDCODING AND ITS"+
 			"SUPPLIERS MAKES NO REPRESENTATIONS OR WARRANTIES OF ANY KIND"+
 			"WITH RESPECT TO THE SERVICE, EITHER EXPRESS OR IMPLIED, AND ALL"+
 			"SUCH REPRESENTATIONS AND WARRANTIES, INCLUDING WARRANTIES"+
 			"OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-"+
 			"INFRINGEMENT, ARE EXPRESSLY DISCLAIMED. WITHOUT LIMITING THE"+
 			"GENERALITY OF THE FOREGOING, CROWDCODING DOES NOT MAKE"+
 			"ANY REPRESENTATION OR WARRANTY OF ANY KIND RELATING TO"+
 			"ACCURACY, SERVICE AVAILABILITY, COMPLETENESS, INFORMATIONAL"+
 			"CONTENT, ERROR-FREE OPERATION, RESULTS TO BE OBTAINED FROM"+
 			"USE, OR NON-INFRINGEMENT. ACCESS AND USE OF THE SERVICE MAY BE"+
 			"UNAVAILABLE DURING PERIODS OF PEAK DEMAND, SYSTEM UPGRADES,"+
 			"MALFUNCTIONS OR SCHEDULED OR UNSCHEDULED MAINTENANCE"+
 			"OR FOR OTHER REASONS. SOME JURISDICTIONS DO NOT ALLOW THE"+
 			"EXCLUSION OF IMPLIED WARRANTIES, SO THE ABOVE EXCLUSION MAY"+
 			"NOT APPLY TO YOU.\n\n"+
 			
 			"8. LIMITATION ON TYPES OF DAMAGES "+
 			"LIMITATION OF LIABILITY\n\n"+
 
 			"IN NO EVENT WILL CROWDCODING BE LIABLE TO YOU OR ANY THIRD"+
 			"PARTY CLAIMING THROUGH YOU (WHETHER BASED IN CONTRACT, TORT,"+
 			"STRICT LIABILITY OR OTHER THEORY) FOR INDIRECT, INCIDENTAL,"+
 			"SPECIAL, CONSEQUENTIAL OR EXEMPLARY DAMAGES ARISING OUT"+
 			"OF OR RELATING TO THE ACCESS OR USE OF, OR THE INABILITY TO"+
 			"ACCESS OR USE, THE SERVICE OR ANY PORTION THEREOF, INCLUDING"+
 			"BUT NOT LIMITED TO THE LOSS OF USE OF THE SERVICE, INACCURATE"+
 			"RESULTS, LOSS OF PROFITS, BUSINESS INTERRUPTION, OR DAMAGES"+
 			"STEMMING FROM LOSS OR CORRUPTION OF DATA OR DATA BEING"+
 			"RENDERED INACCURATE, THE COST OF RECOVERING ANY DATA, THE"+
 			"COST OF SUBSTITUTE SERVICES OR CLAIMS BY THIRD PARTIES FOR"+
 			"ANY DAMAGE TO COMPUTERS, SOFTWARE, MODEMS, TELEPHONES"+
 			"OR OTHER PROPERTY, EVEN IF CROWDCODING HAS BEEN ADVISED OF"+
 			"THE POSSIBILITY OF SUCH DAMAGES. CROWDCODING'S LIABILITY TO"+
 			"YOU OR ANY THIRD PARTY CLAIMING THROUGH YOU FOR ANY CAUSE"+
 			"WHATSOEVER, AND REGARDLESS OF THE FORM OF THE ACTION, IS"+
 			"LIMITED TO THE AMOUNT PAID, IF ANY, BY YOU TO CROWDCODING FOR"+
 			"THE SERVICE IN THE 12 MONTHS PRIOR TO THE INITIAL ACTION GIVING"+
 			"RISE TO LIABILITY. THIS IS AN AGGREGATE LIMIT. THE EXISTENCE OF"+
 			"MORE THAN ONE CLAIM HEREUNDER WILL NOT INCREASE THIS LIMIT.\n\n"+
 
 			"9. Termination\n\n"+
 
 			"CrowdCoding may terminate your access and use of the Service immediately at"+
 			"any time, for any reason, and at such time you will have no further right to use the"+
 			"Service. You may terminate your CrowdCoding account at any time by following the"+
 			"instructions available through the Service. The provisions of these Terms and Conditions"+
 			"relating to the protection and enforcement of CrowdCoding's proprietary rights, your"+
 			"representations and warranties, disclaimer of representations and warranties, release"+
 			"and indemnities, limitations of liability and types of damages, ownership of data and"+
 			"information, governing law and venue, and miscellaneous provisions shall survive any"+
 			"such termination.\n\n"+
 
 			"10. Proprietary Rights in Service Content and Activity"+
 			"Data\n\n"+
 
 			"All content available through the Service, including designs, text, graphics, images,"+
 			"information, software, audio and other files, and their selection and arrangement"+
 			"(the \"Service Content\"), are the proprietary property of CrowdCoding or its licensors. No"+
 			"Service Content may be modified, copied, distributed, framed, reproduced, republished,"+
 			"downloaded, scraped, displayed, posted, transmitted, or sold in any form or by any"+
 			"means, in whole or in part, other than as expressly permitted in these Terms and"+
 			"Conditions. You may not use any data mining, robots, scraping or similar data gathering"+
 			"or extraction methods to obtain Service Content. As between you and CrowdCoding, all"+
 			"data and information generated from your access and use of the activities made available"+
 			"on or through the Service, including code and designs generated by you (collectively,"+
 			"the \"Activity Data\"), shall be exclusively owned by Carnegie Mellon University and/"+
 			"or the University of California at Irvine, and you shall not have any right to use such"+
 			"Activity Data except as expressly authorized by these Terms and Conditions. By using"+
 			"the Service, you hereby assign to Carnegie Mellon University any and all rights, title and"+
 			"interest, including any intellectual property rights or proprietary rights, in the Activity"+
 			"Data. All rights of CrowdCoding or its licensors that are not expressly granted in these"+
 			"Terms and Conditions are reserved to CrowdCoding and its licensors.\n\n"+
 
 			"11. Trademarks\n\n"+
 
 			"\"CrowdCoding\" and all other trademarks, service marks, graphics and logos used in"+
 			"connection with the Service are trademarks or service marks of CrowdCoding or their"+
 			"respective owners, and certain of them are registered with the United States Patent"+
 			"and Trademark Office. Access and use of the Service does not grant or provide you"+
 			"with the right or license to reproduce or otherwise use the CrowdCoding name or any"+
 			"CrowdCoding or third-party trademarks, service marks, graphics or logos.\n\n"+
 
 			"12. Privacy\n\n"+
 
 			"Use of the Service is also governed by our Privacy Policy, a copy of which is located at"+
 			"<URL>. By using the Service, you consent to the terms of the Privacy Policy.\n\n"+
 
 			"13. Governing Law and Arbitration\n\n"+
 
 			"These Terms and Conditions, its subject matter and CrowdCoding's and your respective"+
 			"rights under these Terms and Conditions shall be governed by and construed under the"+
 			"laws of the Commonwealth of Pennsylvania, United States of America, excluding the"+
 			"conflict of law provisions of that or any other jurisdiction. Any dispute arising between"+
 			"you and CrowdCoding will be resolved by binding arbitration. The arbitration shall be"+
 			"conducted by a single arbitrator in the County of Allegheny in the Commonwealth of"+
 			"Pennsylvania. Unless otherwise expressly required by applicable law, each party shall"+
 			"bear its own attorneys' fees without regard to which party is deemed the prevailing party"+
 			"in the arbitration proceeding.\n\n"+
 
 			"14. Language\n\n"+
 
 			"This agreement was originally written in English (US). To the extent any translated"+
 			"version of this agreement conflicts with the English version, the English version controls.\n\n"+
 
 			"15. Miscellaneous\n\n"+
 
 			"These Terms and Conditions constitute the entire agreement between CrowdCoding"+
 			"and you concerning the subject matter hereof. In the event that any of the Terms"+
 			"and Conditions are held by a court or other tribunal of competent jurisdiction to be"+
 			"unenforceable, such provisions shall be limited or eliminated to the minimum extent"+
 			"necessary so that these Terms and Conditions shall otherwise remain in full force and"+
 			"effect. A waiver by CrowdCoding or you of any provision of these Terms and Conditions"+
 			"or any breach thereof, in any one instance, will not waive such term or condition or any"+
 			"subsequent breach thereof. CrowdCoding may assign its rights or obligations under these"+
 			"Terms and Conditions without condition. These Terms and Conditions will be binding"+
 			"upon and will inure to the benefit of CrowdCoding and you, and CrowdCoding's and"+
 			"your respective successors and permitted assigns.\n\n"+
 
 			"Last revised on <date>.\n\n"+
 			
 			"Privacy Policy"+
 			"1. General\n\n"+
 
 			"By using, accessing or participating in the Service, you agree to the terms of this privacy"+
 			"policy (the \"Privacy Policy\"). Capitalized terms not defined in this Privacy Policy have"+
 			"the meanings set forth in the Terms and Conditions, located at <URL>. We reserve the"+
 			"right to change our Privacy Policy at any time. If we do this, we will post a notice that"+
 			"we have made changes to this Privacy Policy on the Website for at least 7 days after the"+
 			"changes are posted and will indicate at the bottom of the Privacy Policy the date these"+
 			"terms were last revised. Any revisions to this Privacy Policy will become effective the"+
 			"earlier of (i) the end of the foregoing 7-day period or (ii) the first time you access or use"+
 			"the Service after any such changes. If you do not agree to abide by this Privacy Policy,"+
 			"you are not authorized to use, access or participate in the Service.\n\n"+
 
 			"2. Information We Collect\n\n"+
 
 			"When you use the Service you provide us with two types of information: (i) information"+
 			"you submit via the Service and (ii) information regarding your use of the Service"+
 			"collected by us as you interact with the Service.\n\n"+
 
 			"When you enter the Website, we may collect your browser type and IP address. This"+
 			"information may be gathered for all Website visitors. In addition, we store certain"+
 			"information from your browser using \"cookies.\" A cookie is a piece of data stored on the"+
 			"user's computer tied to information about the user. We use session ID cookies to confirm"+
 			"that users are logged in. If you do not want information collected through the use of"+
 			"cookies, there is a simple procedure in most browsers that allows you to deny or accept"+
 			"the cookie feature; however, you should note that cookies may be necessary to provide"+
 			"you certain features (e.g., customized delivery of information) available on the Website.\n\n"+
 
 			"Through the registration process you may provide us with your username, nickname, and"+
 			"domain of the Google account you use if authenticated with Google. At other points in"+
 			"the registration or optional profile page completion, you may also provide us with your"+
 			"name, email address, hometown, and other information.\n\n"+
 
 			"When you use the Service you may submit information and content to your profile,"+
 			"generate Activity Data through engaging in activities on the Service, or send messages"+
 			"and otherwise transmit information to other users. We store this information so that we"+
 			"can provide you the Service and offer personalized features.\n\n"+
 
 			"3. Use of Information Obtained by CrowdCoding\n\n"+
 			
 			"We may use your contact information to send you notifications regarding new services"+
 			"offered by CrowdCoding and its partners that we think you may find valuable."+
 			"CrowdCoding may also send you service-related announcements from time to time"+
 			"through the general operation of the Service. Generally, you may opt out of such emails,"+
 			"although CrowdCoding reserves the right to send you notices about your account even if"+
 			"you opt out of all voluntary email notifications.\n\n"+
 
 			"Profile information is used by CrowdCoding primarily to be presented back to and edited"+
 			"by you when you access the Service and to be presented to other visitors. In some cases,"+
 			"other visitors may be able to supplement your profile, including by submitting comments.\n\n"+
 
 			"CrowdCoding may use aggregate or anonymous data collected through the Service,"+
 			"including Activity Data, for any purpose. This data may be used by CrowdCoding and"+
 			"shared with third parties in any manner.\n\n"+
 
 			"4. Sharing Your Personally-Identifiable Information with"+
 			"Third Parties\n\n"+
 
 			"CrowdCoding shares your personally-identifiable information only in limited"+
 			"circumstances where CrowdCoding believes such sharing is reasonably necessary to offer"+
 			"the Service, legally required or, permitted by you. For example:\n\n"+
 
 			"We may provide personally-identifiable information to service providers who help us"+
 			"bring you the Service, such as hosting the Service at a co-location facility or sending"+
 			"email updates. In connection with these operations, our service providers may have"+
 			"access to personally-identifiable information for use for a limited time. Where we utilize"+
 			"service providers for the processing of any of personally-identifiable information,"+
 			"we implement reasonable contractual protections limiting the use of that personally-"+
 			"identifiable information to the provision of services to CrowdCoding.\n\n"+
 
 			"We may be required to disclose personally-identifiable information pursuant to lawful"+
 			"requests, such as subpoenas or court orders, or in compliance with applicable laws."+
 			"Additionally, we may share account or other personally-identifiable information when we"+
 			"believe it is necessary to comply with law, to protect our interests or property, to prevent"+
 			"fraud or other illegal activity perpetrated through the Service or using the CrowdCoding"+
 			"name, or to prevent imminent harm. This may include sharing personally-identifiable"+
 			"information with other companies, lawyers, agents or government agencies.\n\n"+
 
 			"If the ownership of all or substantially all assets owned by CrowdCoding that are"+
 			"related to the Service were to change, your personally-identifiable information may"+
 			"be transferred to the new owner. In any such transfer of information, your personally-"+
 			"identifiable information would remain subject to this section.\n\n"+
 
 			"5. Links\n\n"+
 
 			"The Service may contain links to other websites. We are of not responsible for the"+
 			"privacy practices of other websites. We encourage users to be aware when they leave"+
 			"the Service to read the privacy statements of other websites that collect personally"+
 			"identifiable information. This Privacy Policy applies solely to information collected by"+
 			"CrowdCoding via the Service.\n\n"+
 			
 			"Last revised on <date>."
 			;
 	
 	area.setText(text);
 	area.setReadOnly(true);
 	System.out.println("step3");
 	Button b = new Button("Return");
 	add(b);
 	System.out.println("button added");
 	b.addClickHandler(new ClickHandler(){
 		@Override
 		public void onClick(ClickEvent event) {
		RootPanel.get("ace").clear();
		RootPanel.get("ace").add(new LoginWidget(loginInfo));
 			
 		}
 		
 	});
 	
 	}
 }
