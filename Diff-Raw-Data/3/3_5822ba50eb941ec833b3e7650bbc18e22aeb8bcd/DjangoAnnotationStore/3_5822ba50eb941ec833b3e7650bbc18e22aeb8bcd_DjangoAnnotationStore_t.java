 /*
  *  PathFind -- a Diamond system for pathology
  *
  *  Copyright (c) 2008-2010 Carnegie Mellon University
  *  All rights reserved.
  *
  *  PathFind is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, version 2.
  *
  *  PathFind is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with PathFind. If not, see <http://www.gnu.org/licenses/>.
  *
  *  Linking PathFind statically or dynamically with other modules is
  *  making a combined work based on PathFind. Thus, the terms and
  *  conditions of the GNU General Public License cover the whole
  *  combination.
  *
  *  In addition, as a special exception, the copyright holders of
  *  PathFind give you permission to combine PathFind with free software
  *  programs or libraries that are released under the GNU LGPL or the
  *  Eclipse Public License 1.0. You may copy and distribute such a system
  *  following the terms of the GNU GPL for PathFind and the licenses of
  *  the other code concerned, provided that you include the source code of
  *  that other code when and as the GNU GPL requires distribution of source
  *  code.
  *
  *  Note that people who make modified versions of PathFind are not
  *  obligated to grant this special exception for their modified versions;
  *  it is their choice whether to do so. The GNU General Public License
  *  gives permission to release a modified version without this exception;
  *  this exception also makes it possible to release a modified version
  *  which carries forward this exception.
  */
 
 package edu.cmu.cs.diamond.pathfind;
 
 import java.awt.Shape;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.prefs.Preferences;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.xml.bind.JAXB;
 
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.auth.AuthChallengeParser;
 import org.apache.commons.httpclient.auth.MalformedChallengeException;
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 import edu.cmu.cs.diamond.pathfind.annotations.*;
 import edu.cmu.cs.openslide.gui.Annotation;
 import edu.cmu.cs.openslide.gui.DefaultSelectionListModel;
 import edu.cmu.cs.openslide.gui.SelectionListModel;
 
 public class DjangoAnnotationStore implements AnnotationStore {
 
     private final String uriPrefix;
 
     private final HttpClient httpClient;
 
     DjangoAnnotationStore(HttpClient httpClient, String uriPrefix) {
         if (uriPrefix.endsWith("/")) {
             this.uriPrefix = uriPrefix;
         } else {
             this.uriPrefix = uriPrefix + "/";
         }
         this.httpClient = httpClient;
 
         httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
     }
 
     @Override
     public SelectionListModel getAnnotations(String quickhash1)
             throws IOException {
         SelectionListModel ssModel = new DefaultSelectionListModel();
 
         try {
             URI u = new URI(uriPrefix + quickhash1 + "/");
             System.out.println(u);
 
             GetMethod g = new GetMethod(u.toString());
             try {
                 InputStream in = getBody(g);
                 RegionList rl = JAXB.unmarshal(in, RegionList.class);
 
                 // convert RegionList
                 for (Region r : rl.getRegion()) {
                     Shape s = SlideAnnotation.stringToShape(r.getPath());
                     Integer id = r.getId();
                     String creator = r.getCreator();
 
                     List<SlideAnnotationNote> notes = new ArrayList<SlideAnnotationNote>();
                     for (Note n : r.getNotes().getNote()) {
                         SlideAnnotationNote san = new SlideAnnotationNote(n
                                 .getCreator(), n.getId(), n.getText());
                         notes.add(san);
                     }
 
                     SlideAnnotation sa = new SlideAnnotation(s, id, creator,
                             notes);
                     System.out.println(sa);
                     ssModel.add(sa);
                 }
             } finally {
                 g.releaseConnection();
             }
         } catch (URISyntaxException e) {
             throw new IOException(e);
         }
 
         return ssModel;
     }
 
     private InputStream getBody(GetMethod g) throws IOException {
         g.setDoAuthentication(false);
 
         int code = httpClient.executeMethod(g);
         System.out.println(code);
 
         code = maybeAuthenticate(g, code);
 
         System.out.println("code: " + code);
 
         System.out.println("cookies: "
                 + Arrays.toString(httpClient.getState().getCookies()));
         return g.getResponseBodyAsStream();
     }
 
     private int maybeAuthenticate(HttpMethodBase method, int code)
             throws MalformedChallengeException, IOException, HttpException {
         while (code == 401) {
             // let's authenticate
             Header header = method.getResponseHeader("WWW-Authenticate");
             System.out.println(header);
             String scheme = AuthChallengeParser
                     .extractScheme(header.getValue());
            Map<?, ?> params = AuthChallengeParser.extractParams(header
                    .getValue());
 
             if (!scheme.equals("login")) {
                 throw new IOException(
                         "Authentication requested but of unknown scheme: "
                                 + scheme);
             }
 
             // get the login form
             String loginuri = (String) params.get("realm");
             login(loginuri);
 
             // try again
             code = httpClient.executeMethod(method);
         }
         return code;
     }
 
     final private static Preferences prefs = Preferences
             .userNodeForPackage(DjangoAnnotationStore.class);
 
     private void login(String loginuri) throws IOException {
         String username = prefs.get("username", "");
         String password = "";
 
         JLabel label = new JLabel("Please enter your username and password:");
         JTextField jtf = new JTextField(username);
         JPasswordField jpf = new JPasswordField();
         int dialogResult = JOptionPane.showConfirmDialog(null, new Object[] {
                 label, jtf, jpf }, "Login to PathFind",
                 JOptionPane.OK_CANCEL_OPTION);
         if (dialogResult == JOptionPane.OK_OPTION) {
             username = jtf.getText();
             prefs.put("username", username);
             password = new String(jpf.getPassword());
         } else {
             throw new IOException("User refused to login");
         }
 
         // get the form to get the cookies
         GetMethod form = new GetMethod(loginuri);
         try {
             if (httpClient.executeMethod(form) != 200) {
                 throw new IOException("Can't GET " + loginuri);
             }
         } finally {
             form.releaseConnection();
         }
 
         // get cookies
         String csrftoken = null;
         Cookie[] cookies = httpClient.getState().getCookies();
         for (Cookie c : cookies) {
             System.out.println(c);
             if (c.getName().equals("csrftoken")) {
                 csrftoken = c.getValue();
                 break;
             }
         }
 
         // now, post
         PostMethod post = new PostMethod(loginuri);
         try {
             post.addRequestHeader("Referer", loginuri);
             NameValuePair params[] = { new NameValuePair("username", username),
                     new NameValuePair("password", password),
                     new NameValuePair("csrfmiddlewaretoken", csrftoken) };
             System.out.println(Arrays.toString(params));
             post.setRequestBody(params);
             httpClient.executeMethod(post);
             System.out.println(post.getResponseBodyAsString());
         } finally {
             post.releaseConnection();
         }
     }
 
     @Override
     public void saveAnnotations(String qh1, SelectionListModel ssModel)
             throws IOException {
         // convert to jaxb
         RegionList rl = new RegionList();
         List<Region> regions = rl.getRegion();
         for (Annotation a : ssModel) {
             System.out.println("saving annotation: " + a);
 
             Region r = new Region();
             regions.add(r);
 
             r.setPath(SlideAnnotation.shapeToString(a.getShape()));
 
             if (a instanceof SlideAnnotation) {
                 SlideAnnotation sa = (SlideAnnotation) a;
 
                 r.setId(sa.getId());
 
                 NoteList nl = new NoteList();
                 r.setNotes(nl);
                 List<Note> notes = nl.getNote();
 
                 for (SlideAnnotationNote n : sa.getNotes()) {
                     Note nn = new Note();
                     nn.setId(n.getId());
                     nn.setText(n.getText());
 
                     notes.add(nn);
                 }
             }
         }
 
         // marshal it to a string to post
         StringWriter sw = new StringWriter();
         JAXB.marshal(new ObjectFactory().createRegions(rl), sw);
 
         try {
             String u = new URI(uriPrefix + qh1 + "/", null, null).toString();
             System.out.println(u);
 
             PostMethod post = new PostMethod(u);
             try {
                 post.addRequestHeader("Referer", u);
                 NameValuePair params[] = { new NameValuePair("xml", sw
                         .toString()) };
                 System.out.println(Arrays.toString(params));
                 post.setRequestBody(params);
 
                 int code = httpClient.executeMethod(post);
                 code = maybeAuthenticate(post, code);
 
                 System.out.println(post.getResponseBodyAsString());
             } finally {
                 post.releaseConnection();
             }
         } catch (URISyntaxException e) {
             throw new IOException(e);
         }
     }
 }
