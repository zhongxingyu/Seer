 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.xwiki.contrib.mailarchive.internal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.junit.Test;
 import org.xwiki.contrib.mail.MailItem;
 import org.xwiki.contrib.mailarchive.IType;
 import org.xwiki.contrib.mailarchive.internal.data.Type;
import org.xwiki.contrib.mailarchive.internal.utils.MailUtils;
 import org.xwiki.test.AbstractMockingComponentTestCase;
 import org.xwiki.test.annotation.MockingRequirement;
 
 import com.xpn.xwiki.XWikiException;
 
 /**
  * @version $Id$
  */
 public class MailUtilsTest extends AbstractMockingComponentTestCase
 {

     @MockingRequirement
    private MailUtils mailutils;
 
     private static final String HTML_ENCODED_CONTENT_NO_HISTORY =
         "1F8B0800000000000000ED5A6D6FDB3812FE7C06FC1FA62EB66917962DD94E622B71"
             + "B06DD2F47A688BA2E92138ECDD075AA26D6E28512B5276BD87FB41FB0BEEFBED1FBB"
             + "194AF24BECA469EA6653200862497C9937CE3C9CA10487631349F814C958FB937E2D"
             + "4B635F07631E31ED442248955643E3042AF22791AC15E3D475E3D47028025E5CCA19"
             + "D31BCC98AA342CC78706276422F48F5BA71DCFF55C676FF745DBF1BCD0739EB77AA7"
             + "8EEB3E7FEEBAC75EA7DB6D1593FAB5B13189DF6C4EA7D3C6B4DD50E9A8F9F143F3C3"
             + "CB638774ECB8B5A36AA55A391C7316E2DD61C40D039AE2F05F3331E91FABD8F0D838"
             + "1F670987207FE8D70CFF649A34FF0082314B35377DA195D3EDEEF61CAF36A713B388"
             + "F75FF198A7CCA87431FD6DA9299CA37AE079F07428A4E1290F21E2A1C8A267968836"
             + "33C9E9E691E3542BD0FC114E91049CF0A18885112AD6F063133B7E1A62B3336401AF"
             + "56FEF2EFE2211272E69F8B7814E2BF3EC09E84C54A73C7F377C1BDFC77F09F6AE56A"
             + "32C74C8A412A5688B4C0DB855D68D9BF0EB4E997A890986724F8BA9C49E3AD56EF54"
             + "1A31590729969F4231593C12FB88A52311FB6E1011D7FCC9192863D0371AAEEB7A89"
             + "A10E2BA516BF71DF6B35DCA5B642F2DA4711710DEFF8143EA888C5351291F952C417"
             + "75D0A80C71FD2B2E6E4A4DC4385052A5FE40669C68D14A3B210F142E212AE2677168"
             + "87F29CCE44686178B881D4A992524D79B820996469223F4F3469445A49A14DC25236"
             + "4A5932B6B65A6F24935D6E5D58CE312AB964BD548CC666B345571B251F1ABFBD57DA"
             + "F326C6F73619BF741BD4CADAE765C484B4BEE175ADA41833D6C71D8366F3D1725AC5"
             + "4C5E26F33C1579636EC7A9884335251BCE074EB9552DB6DEB3908C482F355E367BAC"
             + "620EF4B349C0DEED058CD964F60D456BB9D788E6A43C91B33B16F0A7848D389CF180"
             + "BA3C92CE3AC56E6FB7D14E0C743B5EA3B7EC4AFEBE8D55E8B9F6B2F244F4C8B397A9"
             + "1179BF6C2831E60DFAFD0628A470003937113D3AB86578ADBDFD4E6BBFEDF6AC1865"
             + "8FB5DD78864E1AAEB6F32891CC709CAA7DC7EBBAED9EBBBBD769C1DEFE5EAFBBD7ED"
             + "15373DAFBC69DFAECB9AAF90D9977CC2A537179D9E9C388B063C7586B412C61F6452"
             + "723317D58EA0A5F1FF79EABED8BFD4CE06B88C8802769956BA0AA289D2D6763E05FC"
             + "7C9531BC709722AD37C5F4D92C1A28B92E766B55EC39F3FD392CDF8EFD1AA3F6158C"
             + "3CB7BB654E9DAB38753A5BE6B47B15A7AEBB654E7B57706A797B5BE6B47F15A7DD6D"
             + "7B44F72A4EDD6D7B44EF0A4EEDD6D63C6211FE2572EDF5F6BD5ECBEDDD02B9BC76B7"
             + "B5D7F57ADDEEB7042EEFFB042EEFAE80CBBB33E0F2EE0CB8BC3B032EEFCE80CBBB33"
             + "E0F2EE0CB8BC6F0F5C6AA95E5BAE27B02BBBBACB71A8B06D96252EDD173538DE0E54"
             + "3803C9E251FFE53BE7EF674005559F4A3298D8DBBC94CAC762B20881645AF7CB0C31"
             + "6F4F8AD679557974487000949AF65B40656EDFE6C5478794608395A4BFB354D3942E"
             + "BC9649EF1C9D70960293B27EA8FCE4E8B099FF121DBCD078BC247720C77BC999E680"
             + "497008038E052730D0639522796E400D21CD2496BF468161171C448C772C0854169B"
             + "6A653AE631CC54062CE59069118FC08C39BC7A7506E7E77082FE938A414626B5C9B6"
             + "86A7276FCADE339E4E785A87794BB572CCD21054BA6882F76F73556DD5D1A7AA6355"
             + "C74535B273F4EC92F9BED6AE39CFDA636FD8E9ED87B5DCCE45419A4B8185D59AA9F3"
             + "3276530D9BCBFAD83B4572273BB97C4FE2814E0EBE4CCA52F70DA5F6A6F82AF7755F"
             + "7A90EFB22087AA85023CFA590CE191CE920497DBAECFBF720BA05ECB26301C995AED"
             + "F31DF04A375BD7BDD832AB955C79A2B4B33A7D2EDEEB11D68A7CE7E87FFF5D5A052F"
             + "E7BB7606B3C1FEFEBEAD01D786EE1446BEF9AF459595A5289E561A1FFDCCE3500CD1"
             + "64834D7E43AA62C0ADF9CC8DCCB6EA3244AA0CDEA2D646A3863B476F442472B614A8"
             + "54DEEB6BBC7EF0859EBF58F6CF2850ADDC48859DA3D743041712D31E64328125B744"
             + "27E6966F58AD306358308ED07B112934FA6A8A12BC55CFEA406A662997338838C369"
             + "66CC0CFE080D8B39045B9924144311D1959020979A2346A5BC0E0CF14DC54880D9BD"
             + "60697092AA89087988D84658960227111BB02500D9BE191F90E30139BE12394E14C4"
             + "CA40A4508E196EB902FD81FC231BFC8229109037DC6B24392F130F7B5849D909020B"
             + "D79A8D30D443AB5CB53216BFB0E00242A1834C6B4A42CC38C50C5137E01F38354009"
             + "5848619F579834C0A20A075C9F3A60E25290A25734F188239E7C427FB3590E1A88F0"
             + "A230581D144E4BA70233A982874D89D0BA431164D29084437B9E4F4084489761228B"
             + "2C03EC50321F8CB95270C1C3C6BDC51D846F32F994C5561F95E01A3088315E4ABF51"
             + "433A7A2DCD5D87004D617831A8589F83D2A8F3A59B5BB518A1EB68AB941602C39445"
             + "1C4732ADE2FB6B9807407E00E4AF04E45762C22DF40C73206626D394D4D984E4D78C"
             + "6B82A77B0DC9272A7EF2B8DBF2F60F0C85EF885B909807B98DE6B58A907A72FCB5CA"
             + "275C61550E22079AC066685A49348DB503666A03C9232A111140EA08A398D48D1153"
             + "697428424A139F625CC425852196A9B65515A99D9AC6CF1E32BB2520719780A4F300"
             + "24DF3F907CA4539A80F2093650199D9CC443418B2F70BE995DE7FBF701436C669061"
             + "1A757E4E89DB022DA43D3F42B8D09C12A82415134A2DF28F62E61AE6B59BBEC7217E"
             + "42501EB3259D10E537A8807EBC54CE626A6810472515FC58A952BE9B9931E6ED5837"
             + "43FEE65E6F3F3FBAF650113576D734CE3F1378C88D1E206D9B9086D5D70594DFA750"
             + "3AB14815A66305638609C298CB04638176FD0DB836F8CC8ACFBF72FB02E9AB95C5B7"
             + "714B1AACCBFFB7AB052A8C5938CECDE1E61A632E3BE10661EE35FE3F9F28CCE208E1"
             + "A91CA4DC5114EF1EC676A925B7F5E5EAAE6037053603CA3E5BEE81B1DE827E609FBD"
             + "833A4DC60D65094EE9B0301429274CAD560AF288C1014FE3399EDAD3C27BBC91BC8E"
             + "61643F0C95C0ACD9ECB6919F8CD2F66F8F4096CF36D4A290F8FAADE2EB5E3ADD7A53"
             + "F83384300C8B8ECB827CB48D0395863CEDBB1070297172806E5B3C252C0CF3A7A908"
             + "CDB85FF35CF7875AB552CA685B513E14F0871D64048726A50B5EC3724C41835E78C2"
             + "D2FF4E3E2E1493FC667B5602D860A773045795F0A533F7BC3023C4CD5FFE618D35E5"
             + "2095BAA0CA6F4A6FF18C226A741246916C6BAF98CECC6C729347601177C59BBEF564"
             + "EF734EFAED35BF91877C7B315E60D0C2073E42BBEACFBD2E46699A73C7B8CE4306DB"
             + "9471D3A6F7C7EFE91FBF4782C30B9569041E034F58941CC05B36423483F7A9E0597A"
             + "83DDE8C61AFD192BFF3D48D634A1C597A60518BC1074DD005FDBD726A51B772EFB79"
             + "CBCD91B59091EEE87B8DF2230E1349BCFD3F37F8828043320000";
 
     private static final String HTML_DECODED_CONTENT_NO_HISTORY =
         "<htmlxmlns:v=\"urn:schemas-microsoft-com:vml\"xmlns:o=\"urn:schemas-microsoft-com:office:office\"xmlns:w=\"urn:schemas-microsoft-com:office:word\"xmlns:dt=\"uuid:C2F41010-65B3-11d1-A29F-00AA00C14882\"xmlns=\"http://www.w3.org/TR/REC-html40\"><head><metahttp-equiv=Content-Typecontent=\"text/html;charset=iso-8859-1\"><metaname=Generatorcontent=\"MicrosoftWord11(filteredmedium)\"><style><!--/*FontDefinitions*/@font-face{font-family:Wingdings;panose-1:5000000000;}@font-face{font-family:Calibri;panose-1:21552224324;}/*StyleDefinitions*/p.MsoNormal,li.MsoNormal,div.MsoNormal{margin:0cm;margin-bottom:.0001pt;font-size:12.0pt;font-family:\"TimesNewRoman\";}a:link,span.MsoHyperlink{color:blue;text-decoration:underline;}a:visited,span.MsoHyperlinkFollowed{color:purple;text-decoration:underline;}p.msolistparagraph,li.msolistparagraph,div.msolistparagraph{margin-top:0cm;margin-right:0cm;margin-bottom:0cm;margin-left:36.0pt;margin-bottom:.0001pt;font-size:11.0pt;font-family:Calibri;}span.EmailStyle18{mso-style-type:personal;font-family:Arial;color:windowtext;font-weight:normal;font-style:normal;text-decoration:nonenone;}span.EmailStyle19{mso-style-type:personal;font-family:Arial;color:navy;font-weight:normal;font-style:normal;text-decoration:nonenone;}span.EmailStyle20{mso-style-type:personal-reply;font-family:Arial;color:navy;font-weight:normal;font-style:normal;text-decoration:nonenone;}@pageSection1{size:595.3pt841.9pt;margin:72.0pt90.0pt72.0pt90.0pt;}div.Section1{page:Section1;}/*ListDefinitions*/@listl0{mso-list-id:1267427309;mso-list-type:hybrid;mso-list-template-ids:-1803905642676986896769869167698693676986896769869167698693676986896769869167698693;}@listl0:level1{mso-level-number-format:bullet;mso-level-text:\\F0B7;mso-level-tab-stop:none;mso-level-number-position:left;text-indent:-18.0pt;font-family:Symbol;}@listl0:level2{mso-level-tab-stop:72.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl0:level3{mso-level-tab-stop:108.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl0:level4{mso-level-tab-stop:144.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl0:level5{mso-level-tab-stop:180.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl0:level6{mso-level-tab-stop:216.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl0:level7{mso-level-tab-stop:252.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl0:level8{mso-level-tab-stop:288.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl0:level9{mso-level-tab-stop:324.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl1{mso-list-id:1697192099;mso-list-type:hybrid;mso-list-template-ids:1382681988676986896769869167698693676986896769869167698693676986896769869167698693;}@listl1:level1{mso-level-number-format:bullet;mso-level-text:\\F0B7;mso-level-tab-stop:none;mso-level-number-position:left;text-indent:-18.0pt;font-family:Symbol;}@listl1:level2{mso-level-tab-stop:72.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl1:level3{mso-level-tab-stop:108.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl1:level4{mso-level-tab-stop:144.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl1:level5{mso-level-tab-stop:180.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl1:level6{mso-level-tab-stop:216.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl1:level7{mso-level-tab-stop:252.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl1:level8{mso-level-tab-stop:288.0pt;mso-level-number-position:left;text-indent:-18.0pt;}@listl1:level9{mso-level-tab-stop:324.0pt;mso-level-number-position:left;text-indent:-18.0pt;}ol{margin-bottom:0cm;}ul{margin-bottom:0cm;}--></style></head><bodylang=EN-USlink=bluevlink=purple><divclass=Section1><pclass=MsoNormal><fontsize=2face=Arial><spanstyle='font-size:10.0pt;font-family:Arial'>Dearall,<o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2face=Arial><spanstyle='font-size:10.0pt;font-family:Arial'>PleasefindbelowashortsetofrulestotakeintoaccountwhenyouareusingtheGGSWWDistributionLists(DLGGSWWServer,DLGGSWWCardorDLGGSWWPM<fontcolor=navy><spanstyle='color:navy'>)</span></font><o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2color=\"#1f497d\"face=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:#1F497D'><o:p>&nbsp;</o:p></span></font></p><pclass=MsoNormalstyle='margin-left:36.0pt;text-indent:-18.0pt;mso-list:l1level1lfo2'><![if!supportLists]><fontsize=2color=tealface=Symbol><spanstyle='font-size:11.0pt;font-family:Symbol;color:teal'><spanstyle='mso-list:Ignore'>·<fontsize=1face=\"TimesNewRoman\"><spanstyle='font:7.0pt\"TimesNewRoman\"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></font></span></span></font><![endif]><b><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal;font-weight:bold'>Limitsizeofmails<o:p></o:p></span></font></b></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'>Ifamailcontainslargesizedattachments(severalMo),itsurelymeansthatthisattachmentshouldbestoredelsewhere,andonlyalinkshouldbeprovidedinyouremail.<o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'><o:p>&nbsp;</o:p></span></font></p><pclass=MsoNormalstyle='margin-left:36.0pt;text-indent:-18.0pt;mso-list:l1level1lfo2'><![if!supportLists]><fontsize=2color=tealface=Symbol><spanstyle='font-size:11.0pt;font-family:Symbol;color:teal'><spanstyle='mso-list:Ignore'>·<fontsize=1face=\"TimesNewRoman\"><spanstyle='font:7.0pt\"TimesNewRoman\"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></font></span></span></font><![endif]><b><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal;font-weight:bold'>Donotmodifyoriginalsubjecttext<o:p></o:p></span></font></b></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'>Whenyoureplytoamessage,donothijackdiscussionthreads.Youcanaddinformationattheend,butdonotchangeexistingtextinsubject,otherwisethreadsaredifficulttofollowandautomatictoolsaretricked.<o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'>Ifyouwanttoopenanewsubjectofdiscussion,createanewmessage;donotreplytoexistingmessages,forthesamereason.<o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'><o:p>&nbsp;</o:p></span></font></p><pclass=MsoNormalstyle='margin-left:36.0pt;text-indent:-18.0pt;mso-list:l1level1lfo2'><![if!supportLists]><fontsize=2color=tealface=Symbol><spanstyle='font-size:11.0pt;font-family:Symbol;color:teal'><spanstyle='mso-list:Ignore'>·<fontsize=1face=\"TimesNewRoman\"><spanstyle='font:7.0pt\"TimesNewRoman\"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></font></span></span></font><![endif]><b><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal;font-weight:bold'>Givethefinalsatusofyourquestion<o:p></o:p></span></font></b></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'>Don&#8217;tforgettoreplytotheDistributionListtoinformthepeopleifyoucouldsolveyourproblemornot,andhowyoudidit(evenifyoufounditonyourown).<o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'><o:p>&nbsp;</o:p></span></font></p><pclass=MsoNormalstyle='margin-left:36.0pt;text-indent:-18.0pt;mso-list:l0level1lfo4'><![if!supportLists]><fontsize=2color=tealface=Symbol><spanstyle='font-size:11.0pt;font-family:Symbol;color:teal'><spanstyle='mso-list:Ignore'>·<fontsize=1face=\"TimesNewRoman\"><spanstyle='font:7.0pt\"TimesNewRoman\"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></font></span></span></font><![endif]><b><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal;font-weight:bold'>Takecareaboutconfidentiality<o:p></o:p></span></font></b></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'>DonotuseWWdistributionliststosendprivateorconfidentialemails.<o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'>Destinationlistsofconfidentialemailsshouldbestrictlylimitedtoauthorizedpersons.<o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2color=tealface=Arial><spanstyle='font-size:10.0pt;font-family:Arial;color:teal'><o:p>&nbsp;</o:p></span></font></p><pclass=MsoNormalstyle='margin-left:36.0pt;text-indent:-18.0pt;mso-list:l1level1lfo2'><![if!supportLists]><fontsize=2color=tealface=Symbol><spanstyle='font-size:11.0pt;font-family:Symbol;color:teal'><spanstyle='mso-list:Ignore'>·<fontsize=1face=\"TimesNewRoman\"><spanstyle='font:7.0pt\"TimesNewRoman\"'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></font></span></span></font><![endif]><b><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal;font-weight:bold'>Thankpersonallythepeoplewhohavehelpedyou</span></font></b><b><fontsize=2color=tealface=Wingdings><spanstyle='font-size:11.0pt;font-family:Wingdings;color:teal;font-weight:bold'>J</span></font></b><b><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal;font-weight:bold'><o:p></o:p></span></font></b></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'>Avoidsendingrepliestothewholedistributionlisttosay&#8220;thankyou&#8221;,theseshouldbesentdirectlytotheconcernedpersononly.<o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2color=tealface=Calibri><spanstyle='font-size:11.0pt;font-family:Calibri;color:teal'>Ingeneralavoidmailsthatarenotinformationorquestion.<o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2face=Arial><spanstyle='font-size:10.0pt;font-family:Arial'><o:p>&nbsp;</o:p></span></font></p><pclass=MsoNormal><fontsize=2face=Arial><spanstyle='font-size:10.0pt;font-family:Arial'><o:p>&nbsp;</o:p></span></font></p><tableclass=MsoNormalTableborder=0cellspacing=0cellpadding=0width=\"100%\"style='width:100.0%'><tr><tdstyle='padding:0cm0cm0cm0cm'><div><pclass=MsoNormal><fontsize=2face=Arial><spanstyle='font-size:10.0pt;font-family:Arial'>WehopethatthiscouldhelpyouandwelookforwardtoreadingyournextemailsenttotheGGSWWdistributionlists.<o:p></o:p></span></font></p><pclass=MsoNormal><fontsize=2face=Arial><spanstyle='font-size:10.0pt;font-family:Arial'><o:p>&nbsp;</o:p></span></font></p><pclass=MsoNormal><fontsize=2face=Arial><spanstyle='font-size:10.0pt;font-family:Arial'>BestRegards,<o:p></o:p></span></font></p></div><div><pclass=MsoNormal><b><fontsize=2face=Arial><spanstyle='font-size:10.0pt;font-family:Arial;font-weight:bold'>JérémieBousquet&amp;MagaliPrieur<o:p></o:p></span></font></b></p></div><div><pclass=MsoNormal><fontsize=2face=Arial><spanstyle='font-size:10.0pt;font-family:Arial'><o:p>&nbsp;</o:p></span></font></p></div><div><pclass=MsoNormal><fontsize=2face=Arial><spanstyle='font-size:10.0pt;font-family:Arial'><o:p>&nbsp;</o:p></span></font></p></div></td></tr></table><pclass=MsoNormal><fontsize=3face=\"TimesNewRoman\"><spanstyle='font-size:12.0pt'><o:p>&nbsp;</o:p></span></font></p></div></body></html>";
 
     @Test
     public void decodeMailContentForNoHtmlAndNoBodyAndNoCut() throws IOException, XWikiException
     {
         String decoded = this.mailutils.decodeMailContent("", "", false);
         assertEquals("", decoded);
     }
 
     @Test
     public void decodeMailContentForNoHtmlAndBodyAndNoCut() throws IOException, XWikiException
     {
         String decoded = this.mailutils.decodeMailContent("", "some text...", false);
         assertEquals("some text...", decoded);
     }
 
     @Test
     public void decodeMailContentForNoHtmlAndBodyAndCut() throws IOException, XWikiException
     {
         // with nothing to cut from body
         String decoded = this.mailutils.decodeMailContent("", "some text...", true);
         assertEquals("some text...", decoded);
 
         // with a body to cut
         decoded = this.mailutils.decodeMailContent("", "some text...\nFrom: Michel\nSome historic content", true);
         assertEquals("some text...\n", decoded);
 
         // with a body to cut (and pattern on same line)
         decoded = this.mailutils.decodeMailContent("", "some text...  From: Michel\nSome historic content", true);
         assertEquals("some text...  From: Michel\nSome historic content", decoded);
 
         // with a body to cut (and only blank characters at beginning of line to cut)
         decoded = this.mailutils.decodeMailContent("", "some text...\n   \tFrom: Michel\nSome historic content", true);
         assertEquals("some text...\n", decoded);
     }
 
     @Test
     public void decodeMailContentForHtmlAndNoBodyAndNoCut() throws IOException, XWikiException
     {
         String decoded = this.mailutils.decodeMailContent(HTML_ENCODED_CONTENT_NO_HISTORY, "", false);
 
         // To avoid escaping result string for java, we remove white-space from both result and expected
         assertEquals(HTML_DECODED_CONTENT_NO_HISTORY, decoded.replaceAll("\\s", ""));
     }
 
     @Test
     public void decodeMailContentForHtmlAndBodyAndNoCut() throws IOException, XWikiException
     {
         String decoded = this.mailutils.decodeMailContent(HTML_ENCODED_CONTENT_NO_HISTORY, "ignored text", false);
 
         // To avoid escaping result string for java, we remove white-space from both result and expected
         assertEquals(HTML_DECODED_CONTENT_NO_HISTORY, decoded.replaceAll("\\s", ""));
     }
 
     @Test
     public void decodeMailContentForHtmlAndNoBodyAndCut() throws IOException, XWikiException
     {
         fail("To be implemented");
     }
 
     @Test
     public void extractTypesWithLimitValues()
     {
         try {
             mailutils.extractTypes(null, null);
         } catch (IllegalArgumentException e) {
             // ok
         }
 
         try {
             List<IType> types = new ArrayList<IType>();
             mailutils.extractTypes(types, null);
         } catch (IllegalArgumentException e) {
             // ok
         }
 
         try {
             MailItem mail = new MailItem();
             mailutils.extractTypes(null, mail);
         } catch (IllegalArgumentException e) {
             // ok
         }
 
     }
 
     @Test
     public void extractTypesForNominalCases_MailType()
     {
         // Check with a unique "mail" type
         List<IType> types = new ArrayList<IType>();
 
         Type typeMail = new Type();
         typeMail.setName(IType.TYPE_MAIL);
         typeMail.setDisplayName("Mail");
         HashMap<List<String>, String> patterns = new HashMap<List<String>, String>();
         List<String> fields = new ArrayList<String>();
         fields.add("subject");
         patterns.put(fields, "^.*$");
         typeMail.setPatterns(patterns);
         typeMail.setIcon("email");
         types.add(typeMail);
 
         MailItem m = new MailItem();
         m.setSubject("lorem ipsum");
 
         List<IType> foundTypes;
         foundTypes = mailutils.extractTypes(types, m);
         assertNotNull(foundTypes);
         assertEquals(1, foundTypes.size());
         assertEquals(typeMail, foundTypes.get(0));
 
     }
 
     @Test
     public void extractTypesForNominalCases_OtherType()
     {
         // Check with a unique "mail" type
         List<IType> types = new ArrayList<IType>();
 
         Type typeProposal = new Type();
         typeProposal.setName("proposal");
         typeProposal.setDisplayName("Proposal");
         HashMap<List<String>, String> patterns = new HashMap<List<String>, String>();
         List<String> fields = new ArrayList<String>();
         fields.add("subject");
         patterns.put(fields, "(?mi)^.*\\[proposal\\].*$");
         typeProposal.setPatterns(patterns);
         typeProposal.setIcon("proposal");
         types.add(typeProposal);
 
         List<IType> foundTypes;
 
         // Non-matching test
         MailItem m = new MailItem();
         m.setSubject("lorem ipsum");
 
         foundTypes = mailutils.extractTypes(types, m);
         assertNotNull(foundTypes);
         assertEquals(0, foundTypes.size());
 
         // Matching test
         m.setSubject("[xwiki-user][Proposal] Add more unitary tests");
         foundTypes = mailutils.extractTypes(types, m);
         assertNotNull(foundTypes);
         assertEquals(1, foundTypes.size());
         assertEquals(typeProposal, foundTypes.get(0));
 
         // With pattern at line start - non-matching test
         patterns.put(fields, "(?mi)^\\[proposal\\].*$");
         foundTypes = mailutils.extractTypes(types, m);
         assertNotNull(foundTypes);
         assertEquals(0, foundTypes.size());
 
         // With pattern at line start - matching test
         m.setSubject("[prOposaL] Too low");
 
         foundTypes = mailutils.extractTypes(types, m);
         assertNotNull(foundTypes);
         assertEquals(1, foundTypes.size());
         assertEquals(typeProposal, foundTypes.get(0));
 
     }
 
     @Test
     public void extractTypesForMultiplePatternsAndTypes()
     {
         // Check with a unique "mail" type
         List<IType> types = new ArrayList<IType>();
 
         // First setup a type "proposal" that matches subject field
         Type typeProposal = new Type();
         typeProposal.setName("proposal");
         typeProposal.setDisplayName("Proposal");
         HashMap<List<String>, String> patterns = new HashMap<List<String>, String>();
         List<String> fields = new ArrayList<String>();
         fields.add("subject");
         patterns.put(fields, "(?mi)^.*\\[proposal\\].*$");
         typeProposal.setPatterns(patterns);
         typeProposal.setIcon("proposal");
         types.add(typeProposal);
 
         // Type release : matches subject for a token, and a specific originating from
         Type typeRelease = new Type();
         typeRelease.setName("release");
         typeRelease.setDisplayName("Release");
         HashMap<List<String>, String> patternsRelease = new HashMap<List<String>, String>();
         List<String> fieldsReleaseSubject = new ArrayList<String>();
         fieldsReleaseSubject.add("subject");
         patternsRelease.put(fieldsReleaseSubject, "(?mi)^\\[release\\].*$");
         List<String> fieldsReleaseFrom = new ArrayList<String>();
         fieldsReleaseFrom.add("from");
         patternsRelease.put(fieldsReleaseFrom, "(?mi)^.*vmassol.*$");
         typeRelease.setPatterns(patternsRelease);
         typeRelease.setIcon("release");
         types.add(typeRelease);
 
         List<IType> foundTypes;
 
         // Non-matching test
         MailItem m = new MailItem();
         m.setSubject("lorem ipsum");
         m.setFrom("toto");
 
         foundTypes = mailutils.extractTypes(types, m);
         assertNotNull(foundTypes);
         assertEquals(0, foundTypes.size());
 
         // Match only proposal type
         m.setSubject("[Proposal] This is a proposal");
         m.setFrom("vmassol");
         foundTypes = mailutils.extractTypes(types, m);
         assertNotNull(foundTypes);
         assertEquals(1, foundTypes.size());
         assertEquals(typeProposal, foundTypes.get(0));
 
         // Match only release type
         m.setSubject("[Release] This is a release");
         m.setFrom("Vincent Massol <vmassol@mailarchive.net");
         foundTypes = mailutils.extractTypes(types, m);
         assertNotNull(foundTypes);
         assertEquals(1, foundTypes.size());
         assertEquals(typeRelease, foundTypes.get(0));
 
         // Match both types
         m.setSubject("[Release] [PROPOSAL] A new released proposal, whatever it could mean");
         m.setFrom("vmassol@xwiki.xwiki");
         foundTypes = mailutils.extractTypes(types, m);
         assertEquals(2, foundTypes.size());
         if ((!typeProposal.equals(foundTypes.get(0)) && !typeRelease.equals(foundTypes.get(0)))
             || (!typeProposal.equals(foundTypes.get(1)) && !typeRelease.equals(foundTypes.get(1)))) {
             fail("Invalid types found");
         }
     }
 }
