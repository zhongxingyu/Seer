 /**
  * Copyright (c) 2008 Devin Weaver
  * Licensed under the Educational Community License version 1.0
  * See the file COPYING with this distrobution for details.
  */
 package com.tritarget.client;
 
 import com.google.gwt.user.client.ui.HTML;
 
 /**
  * Static class for the About Box HTML template.
  */
 public final class AboutBox {
     public static HTML getHtml() {
         return new HTML("<div class=\"about-box\">"
                         + "<h4>" + CipherEncoder.APP_NAME
                         + " Version " + CipherEncoder.APP_VERSION + "</h4>"
                         + "<p>by Devin Weaver "
                         + "&lt;<a href=\"mailto:weaver.devin" + '@' + "gmail.com\">"
                         + "weaver.devin" + '@' + "gmail.com</a>&gt;.</p>"
                         + "<p>This application will encode and decode the "
                         + "<a href=\"http://www.everything2.com/node/811165\">Double Box Playfair cipher</a>"
                        + " and the <a href=\"http://en.wikipedia.org/wiki/Transposition_cipher#Double_transposition\">Double Tansposition cipher</a>.</p>"
                         + "<p>I started really getting into pen/paper ciphers and the Playfair cipher really interested me. But then I started seeing flaws and so latched on to the Double Box cipher. Then I saw the Double Transposition cipher and really like that but it used the same letters only jumbled up. I thought gee what if the two were put together. Later on I wanted to learn <a href=\"http://code.google.com/webtoolkit/\">Google Web Toolkit</a> and thought if I wrote an application for the two ciphers I would have the perfect opportunity to dive into the deep end and learn it first hand.</p>"
                         + "<p>This is my first GWT application hope you enjoy it as much as I did making it.</p>"
                         + "<p>Source is avaliable at <a href=\"http://svn.tritarget.org/websvn/listing.php?repname=CipherEncoder&path=%2Ftrunk%2F#_trunk_\">svn.tritarget.org</a>.</p>"
                         + "<p class=\"copyright\">Copyright &copy; 2008 Devin Weaver<p><p class=\"copyright\">Licensed under the <a href=\"http://www.opensource.org/licenses/ecl1.php\">Educational Community License</a> version 1.0</p>"
                         + "</div>");
     }
 }
