 //
 // $Id$
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: 2006-05-04
 // Creator: Etienne Gauthier
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.policies;
 
 import java.io.File;
 
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 
 import wjhk.jupload2.JUploadApplet;
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.filedata.FileData;
 import wjhk.jupload2.gui.JUploadFileFilter;
 import wjhk.jupload2.gui.JUploadFileView;
 import wjhk.jupload2.gui.JUploadPanel;
 
 /**
  * This package contains upload policies, which allow easy configuration of the
  * applet behaviour. <br>
  * <br>
  * The class {@link DefaultUploadPolicy} contains a default implementation for
  * all UploadPolicy methods. <br>
  * <br>
  * <h4><a name="parameters">Parameters</a></h4>
  * <!-- ATTENTION: The following comment is used by Ant build. DO NOT CHANGE!!
  * --> <!-- ANT_COPYDOC_START -->
  * <p>
  * Here is the list of all parameters available in the current package, that is:
  * available in available upload policies. These are applet parameters that
  * should be 'given' to the applet, with <PARAM> tags, as precised below in the
  * <a href="#example">example</a>.
  * </p>
  * <table border="1">
  * <tr>
  * <th>Parameter name</th>
  * <th>Default value / <br>
  * Implemented in</th>
  * <th>Description</th>
  * </tr>
  * <tr>
  * <td>afterUploadTarget<br>
  * Since 2.9.2rc4</td>
  * <td>_self<br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This parameter allows to select a specific target frame when redirecting
  * to <code>afterUploadURL</code>. The following values are possible:<br>
  * <ul>
  * <li><code>_self</code> - Show in the window and frame that contain the
  * applet. </li>
  * <li><code>_parent</code> - Show in the applet's parent frame. If the
  * applet's frame has no parent frame, acts the same as <i>_self</i>.</li>
  * <li><code>_top</code> - Show in the top-level frame of the applet's
  * window. If the applet's frame is the top-level frame, acts the same as
  * <i>_self</i>.</li>
  * <li><code>_blank</code> - Show in a new, unnamed top-level window.
  * <li><i>name</i> - Show in the frame or window named <i>name</i>. If a
  * target named <i>name</i> does not already exist, a new top-level window with
  * the specified name is created, and the document is shown there.</li>
  * </ul>
  * See also:
  * {@link java.applet.AppletContext#showDocument(java.net.URL, java.lang.String)}
  * </td>
  * </tr>
  * <tr>
  * <td>afterUploadURL</td>
  * <td><i>null</i><br>
  * since 2.9.0<br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This parameter is used by all policies. It allows the applet to change
  * the current page to another one after a successful upload. <br>
  * This allows, for instance, to display a page containing the file description
  * of the newly uploaded page. </td>
  * </tr>
  * <tr>
  * <td>albumId</td>
  * <td>-1 <br>
  * <br>
  * {@link wjhk.jupload2.policies.CoppermineUploadPolicy}</td>
  * <td>This parameter is only used by CoppermineUploadPolicy. So it is to be
  * used to upload into a <a href="http://coppermine.sourceforge.net/">coppermine
  * picture gallery</a>. This parameter contains the identifier of the album,
  * where pictures should be used. See CoppermineUploadPolicy for an example.
  * <br>
  * Before upload, CoppermineUploadPolicy.{@link wjhk.jupload2.policies.CoppermineUploadPolicy#isUploadReady()}
  * checks that the albumId is correct, that is: >=1. </td>
  * </tr>
  * <tr>
  * <td>allowHttpPersistent</td>
  * <td><i>true</i><br>
  * since 3.0.0rc1<br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This parameter allows to switch off persistent HTTP connections which
  * are enabled by default (and the protocol version allows it). Currently, we
  * encountered problems with persistent connections when testing on a windows
  * box using a loopback interface only.</td>
  * </tr>
  * <tr>
  * <td>allowedFileExtensions</td>
  * <td><i>empty string</i><br>
  * since 2.9.0<br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This parameter allows the caller to specify a list of file extension. If
  * this parameter is specified, only file with this extension can be selected in
  * the applet.<br>
  * This parameter must contains a list of extensions, in lower case, separated
  * by slashes. eg: jpg/jpeg/gif </td>
  * </tr>
  * <tr>
  * <td>debugLevel</td>
  * <td>0 <br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>With 0, you get the normal production output. The higher the number is,
  * the more information is displayed in the log window. <br>
  * Note: All debug messages are stored in a temporary log file. This can be used
  * to display more information, if needed. See also the 'webmasterMail'
  * parameter. </td>
  * </tr>
  * <tr>
  * <td>filenameEncoding</td>
  * <td><i>null</i><br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>With null, the filename in the <i>Content-Disposition</i> header is not
  * encoded. If not null, the applet tries to encode this filename with the given
  * encoding. It's up to the receiver (the web site) to decode this encoding (see
  * {@link #getUploadFilename(FileData, int)}. <br>
  * Example: if the "UTF8" encoding is choosen, the PHP function urldecode can be
  * used to decode the filename. </td>
  * </tr>
  * <tr>
  * <tr>
  * <td>formdata</td>
  * <td><i>null</i><br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}<br>
  * <i>Since 2.9.2rc4</i></td>
  * <td>With this parameter, the name of a HTML form can be specified. If the
  * specified form exists in the same document like the applet, all all
  * form-variables are added as POST parameters to the applet's POST request.</td>
  * </tr>
  * <tr>
  * <td>highQualityPreview</td>
  * <td>false<br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>If this parameter is set to <i>true</i>, the applet will call the
  * BufferedImage.getScaledInstance(), instead of doing a basic scale
  * transformation. This consume more CPU: on a PII 500MHz, the full screen go
  * from around 5 seconds to between 12 and 20 seconds, for a picture created by
  * my EOS20D (8,5M pixels). The standard preview (above the file list) seem to
  * be displayed at the same speed, whatever is the value of this parameter. <br>
  * Note: when resizing is done before upload, the
  * BufferedImage.getScaledInstance() is always called, so that the uploaded
  * picture is of the best available quality. </td>
  * </tr>
  * <tr>
  * <td>lang</td>
  * <td>Navigator language <br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>Should be something like <i>en</i>, <i>fr</i>... Currently only french
  * and english are known from the applet. If anyone want to add another language
  * ... Please translate the wjhk.jupload2.lang.lang_en, and send it back to
  * <mailto:etienne_sf@sourceforge.net">. </td>
  * </tr>
  * <tr>
  * <td>lookAndFeel <br>
  * since 2.5</td>
  * <td><i>empty</i><br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This allows to control the look &amp; feel of the applet. The authorized
  * values are:
  * <ul>
  * <li><i>empty</i>: uses the default look &amp; feel. This is the same as
  * java.
  * <li>java: uses the java default look &amp; feel. Same as <i>empty</i>.
  * <li>system: uses the current system look and feel. The call will be : <br>
  * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
  * <li>Any valid String argument for UIManager.setLookAndFeel(String).
  * </ul>
  * </td>
  * </tr>
  * <tr>
  * <td>maxChunkSize<br>
  * Since 2.7.1</td>
  * <td>0<br>
  * <i>Long.MAX_VALUE</i><br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This parameters defines the maximum size of an upload.
  * <ul>
  * <li>If not set, or set to a value of 0 or less, the chunk mode is disabled.
  * That is: each file will be uploaded within being splitted in pieces.
  * <li>If set to a value of 1 or more, the upload size will be never be more
  * than maxChunkSize. A file bigger will be split in several part of
  * <i>maxChunkSize</i> size, then the last part will contain the remaining, and
  * will probably be smaller than <i>maxChunkSize</i>.
  * </ul>
  * <br>
  * <b>How to build the server part:</b> the server will have to 'guess' that
  * the file is splitted, and then it will have to reconstruct the uploaded file.
  * Here are the necessary informations:
  * <ul>
  * <li>When a file is chunked, the <i>jupart</i> and <i>jufinal</i> parameter
  * are given in the URL (get parameters). This identify a chunk upload. If these
  * parameters are not given, the file(s) is(are) uploaded in one piece.
  * <li><i>jupart</i> identify the part number: from 1 to N for a file being
  * plitted in N pieces. The N-1 chunks should be <i>maxChunkSize</i> bytes
  * long. The last one contains the remaining of the file.
  * <li><i>jufinal</i> is set to 0 for chunks from 1 to N-1. It is is set to 1
  * only for the last chunk (N, in this 'example').
  * <li>The uploaded filename is not modified when the upload is chunked.
  * Example: upload of the file <i>bigPicture.jpeg</i>, of 4,5 Mb, with chunk of
  * 2Mb. The upload is splitted in three chunk. Chunk 1 and 2 are 2Mb long. The
  * third one is 0,5Mb long. The uploaded filename for these three uploads is
  * <i>bigPicture.jpeg</i>. It's up to the server part to read the <i>jupart</i>
  * and <i>jufinal</i> get parameters, to understand that the upload is chunked.
  * <li><b>Important:</b> The server script <u>must</u> check the resulting
  * filesize. If not, the client can send a file of any size, and fill the server
  * hard drive.
  * <li>The wwwroot/pages/parseRequest.jsp is a java example of a server page
  * that can receive chunk upload. It stores each chunk is <i>filename.partN</i>
  * (where N is the chunk number), then construct the final file, by
  * concatenating all parts together.
  * </ul>
  * <b>Note: </b> If nbFilesPerRequest is different than 1, the applet will try
  * to upload the files until the sum of their content length is less than
  * maxChunkSize. The upload is triggered just before the sum of their content
  * length is bigger then maxChunkSize.<br>
  * If one file is bigger than <i>maxChunkSize</i>, all previous files are
  * uploaded (at once or not, depending on nbFilesPerRequest). Then the 'big'
  * file is uploaded alone, splitted in chunk. Then upload goes on, file by file
  * or not, depending on <i>nbFilesPerRequest</i>. </td>
  * </tr>
  * <tr>
  * <td>maxFileSize<br>
  * Since 2.7.1</td>
  * <td>0<br>
  * <i>Long.MAX_VALUE</i><br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This parameter identify the maximum size that an uploaded file may have.
  * It prevent the user to upload too big files. It is especially important when
  * chunk upload is activated (see below <i>maxChunkSizew</i>).
  * <ul>
  * <li>If <i>maxChunkSize</i> is not set, negative or 0, <i>maxFileSize</i>
  * should be the maximum upload size of the server. In this case, it is useful
  * only to display a message when the user select a file that will be refused by
  * the server.
  * <li>If chunk upload is activated, this parameter becomes really important:
  * in this case the maximum file size of an uploaded file is ... the available
  * space on the server hard drive! (see below, <i>maxChunkSize</i>).
  * </ul>
  * </td>
  * </tr>
  * <tr>
  * <td>maxPicHeight</td>
  * <td>-1 <br>
  * <br>
  * {@link wjhk.jupload2.policies.PictureUploadPolicy}</td>
  * <td>This parameters allows the HTML page to control the maximum height for
  * pictures. If a picture is to be download, and its height is bigger, the
  * picture will be resized. The proportion between width and height of the
  * resized picture are the same as those of the original picture. If both
  * maxPicHeight and maxPicWidth are given, it can happen that the resized
  * picture has a height lesser than maxPicHeight, so that width is no more than
  * maxPicWidth. <br>
  * <b>Precisions:</b> <br>
  * If this parameter value is negative, then no control is done on the picture
  * height. <br>
  * If the original picture is smaller than the maximum size, the picture is not
  * enlarged. <br>
  * If the picture is resized, its other characteristics are kept (number of
  * colors, ColorModel...). The picture format is ketp, if targetPictureFormat is
  * empty. If the picture format is a destructive (like jpeg), the maximum
  * available quality is choosed. <br>
  * <i>See also maxPicWidth, realMaxPicHeight</i> </td>
  * </tr>
  * <tr>
  * <td>maxPicWidth</td>
  * <td>-1 <br>
  * <br>
  * {@link wjhk.jupload2.policies.PictureUploadPolicy}</td>
  * <td>Same as maxPicHeight, but for the maximum width of the uploaded picture.
  * <br>
  * <i>See also maxPicHeight, realMaxPicWidth</i> </td>
  * </tr>
  * <tr>
  * <td>nbFilesPerRequest</td>
  * <td>-1 <br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This allows the control of the maximal number of files that are uploaded
  * in one HTTP upload to the server. <br>
  * If set to -1, there is no maximum. This means that all files are uploaded in
  * the same HTTP request. <br>
  * If set to 5, for instance, and there are 6 files to upload, there will be two
  * HTTP upload request to the server : 5 files in the first one, and that last
  * file in a second HTTP request. </td>
  * </tr>
  * <tr>
  * <td>postURL</td>
  * <td>null since 1.9.2rc4, (was <i>Mandatory</i> before)<br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This parameter specifies the target URL toward which the files should be
  * uploaded. Since version 1.9.2rc4 this parameter is not mandatory anymore.
  * Instead, if omitted or a <i>relative</i> URL is given, the resulting URL is
  * constructed from the applet's DocumentBaseURL. This means, that if the applet
  * tag is dynamically constructed from a PHP script without specifying
  * <code>postURL</code>, the <i>same</i> same script receives the subsequent
  * POST request(s). If this URL may change during the applet execution time, you
  * can call the setProperty applet method from javascript, or create a new
  * UploadPolicy class and either :
  * <ul>
  * <li>Override the {@link wjhk.jupload2.policies.UploadPolicy#getPostURL()}
  * method, to make the postURL totaly dynamic.
  * <li>Override the
  * {@link wjhk.jupload2.policies.UploadPolicy#setPostURL(String)} method, to
  * modify the postURL on the fly, when it is changed.
  * <li>Override the
  * {@link wjhk.jupload2.policies.UploadPolicy#setProperty(String, String)}
  * method. The {@link wjhk.jupload2.policies.CoppermineUploadPolicy} changes the
  * postURL when the albumID property changes.
  * <li>Find another solution ...
  * </ul>
  * <u>Note 1:</u> in HTTP, the upload is done in the same user session, as the
  * applet uses the cookies from the navigator. This allows right management
  * during upload, on the server side.<br>
  * <u>Note 2:</u> FTP URL should looks like:
  * ftp://username:password@myhost.com:21/directory<br>
  * <u>Note 3:</u> in FTP, you'll have to add the jakarta-commons-oro.jar and
  * jakarta-commons-net.jar jar files in the applet ARCHIVE tag attribute. See
  * the 'advanced_js_demo.html page for a sample. You'll have to put the two
  * files coming from the JUpload distribution in the same directory as the
  * wjhk.jupload.jar.</td>
  * </tr>
  * <tr>
  * <td>realMaxPicHeight</td>
  * <td>-1 <br>
  * <br>
  * {@link wjhk.jupload2.policies.PictureUploadPolicy}<br>
  * <i>Since v2.8.1</i></td>
  * <td>This parameters is about the same as maxPicHeight. It overrides it for
  * pictures that must be transformed (currentlty only when the picture is
  * rotated). <br>
  * The aim of this parameter, is to prevent the applet to resize picture, and
  * let the server do it: it will be much quicker. <br>
  * This allows you to:
  * <ul>
  * <li>Put a 'big' <i>maxPicHeight</i> (or don't provide the parameter in the
  * APPLET tag), and let the server resize the picture according to the real
  * maxHeight. The <i>maxPicHeight</i> will be used when the picture is not
  * tranformed by the user.
  * <li>Put this realMaxHeight to the real configured maxHeight. The applet will
  * then directly produce the final file, when it has to tranform the picture
  * (picture rotation, for instance).
  * </ul>
  * <br>
  * <i>See also maxPicHeight, realMaxPicWidth, maxChunkSize (to override any
  * server upload size limitation).</i> </td>
  * </tr>
  * <tr>
  * <td>realMaxPicWidth</td>
  * <td>-1 <br>
  * <br>
  * {@link wjhk.jupload2.policies.PictureUploadPolicy}<br>
  * <i>Since v2.8.1</i></td>
  * <td>Same as realMaxPicHeight, but for the maximum width of uploaded picture
  * that must be transformed. <br>
  * <i>See also maxPicWidth, realMaxPicHeight</i> </td>
  * </tr>
  * <tr>
  * <td>serverProtocol</td>
  * <td>null since 2.9.2rc4<br>
  * (before: "HTTP/1.1")<br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This parameter allows the control of the protocol toward the server.
  * Currently, only HTTP is supported, so valid values are HTTP/0.9 (not tested),
  * HTTP/1.0 and HTTP/1.1. Since version 2.9.2rc4, the default is <i>null</i>,
  * introducing a new facility of automatically adjusting the protocol according
  * to the server response.<br>
  * This parameter is really useful only in
  * {@link wjhk.jupload2.policies.CoppermineUploadPolicy}, as the coppermine
  * application also controls that the requests send within an HTTP session uses
  * the same HTTP protocol (as a protection to limit the 'steal' of session
  * cookies). </td>
  * </tr>
  * <tr>
  * <td>showLogWindow<br>
  * Since 3.0.2</td>
  * <td>True<br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This parameter was formerly known as <i>showStatusBar</i> which now has
  * a different purpose. If given with the <i>False</i> value, the log window
  * will be hidden. The applet will still store all debug information in it. But
  * the user won't see it any more. If a problem occurs, the <i>urlToSendErrorTo</i>
  * can still be used to log all available information. </td>
  * </tr>
  * <tr>
  * <td>showStatusBar</td>
  * <td>True<br>
  * <br>
  * New meaning since 3.0.2<br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This parameter controls if the status bar is shown in the applet. If
  * shown, the stausbar provides information about the current transfer speed and
  * estimated time of completion. Before version 3.0.2, this parameter was used
  * to control visibility of the log window. This is now controlled by
  * <i>showLogWindow</i>.</td>
  * </tr>
  * <tr>
 * <td>sslVerifyCert<br>Since 3.0.2b1</td>
  * <td>none<br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>With this parameter, the handling of certificates when using SSL can be
  * configured. There are 4 possible settings:<br>
  * <ul>
 * <li><i>none</i> (default): Any server cert is accepted, not client cert is
 * used.</li>
  * <li><i>server</i>: The server cert is verified against the local truststore
  * and if that fails, a dialog pops up which asks the user if the certificate
  * shall be accepted permanently, just for the current session or not at all
  * (Just like any browser would do).</li>
  * <li><i>client</i>: A user-certificate (which must be available in the local
 * keystore) is used to perform client-authentication.</li>
  * <li><i>strict</i>: The combination of <i>client</i> and <i>server</i>.</li>
  * </ul>
  * <p>
  * The location of the local truststore and keystore uses the normal JRE
  * conventions. This means, that the system truststore is used for verifying
  * server certs (usually in $JAVA_HOME/lib/security/cacerts) unless either the
  * system property <i>javax.net.ssl.trusStore</i> specifies another location or
  * a file <b>.truststore</b> exists in the user's home directory. If the user
  * decides to permanently accept an untrusted certificate, the file
  * <b>.truststore</b> in the user's home directory is written. The default
 * keystore (for client certificates) is the file <b>.keystore</b> in the user's
 * home directory. This can be overridden by setting the system property
  * <i>javax.net.ssl.keyStore</i>. If the name of the keystore ends in <b>.p12</b>,
  * it is assumed that the keystore is in <b>PKCS12</b> format, otherwise the
  * default format as specified in the JRE security-configuration is used.
  * <p>
 * <b>Important Note:</b>
  * <p>
  * At the time of this writing, a <i>serious</i> bug exists in apache 2.0.x
  * which prevents POST requests when SSL renegotiation is about to happen.
  * Renegotiation is triggered by a location-based (or directory-based) change of
  * the SSLVerifyClient directive in apache. Therefore you <b>can not</b>
  * protect a sub-area of an otherwise unprotected SSL server. You can circumvent
  * that by setting up a virtualhost which is configured to perform SSL client
 * verification <b>for the complete</b> virtualhost. It is the understanding of
 * the author, that this bug has been fixed for apache 2.2. However the author
 * did not verify that.</td>
  * </tr>
  * <tr>
  * <td>storeBufferedImage</td>
  * <td>false <br>
  * <br>
  * {@link wjhk.jupload2.policies.PictureUploadPolicy}</td>
  * <td>This parameter indicates that the preview image on the applet is kept in
  * memory. It works really nice under eclise. But, once in the navigator, the
  * applet runs very quickly out of memory. So I add a lot of calls to
  * {@link wjhk.jupload2.filedata.PictureFileData#freeMemory(String)}, but it
  * doesn't change anything. Be careful to this parameter, and let it to the
  * default value, unless you've well tested it under all your target client
  * configurations. </td>
  * </tr>
  * <tr>
  * <td>stringUploadError</td>
  * <td>empty string ("") [if using DefaultUploadPolicy]<br>
  * "ERROR (.*)" [if using CopperminUploadPolicy]<br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}<br>
  * Since 2.9.2rc4</td>
  * <td>This string is a regular expression. It allows the applet to test that
  * the server has detected an error in the upload. If this parameter is given to
  * the applet, the upload thread will try to match this regular epression to
  * each line of the server response <b>body</b>.<br>
  * If the match is successfull once, the upload is considered to have failed.
  * and {@link wjhk.jupload2.exception.JUploadExceptionUploadFailed} is thrown.
  * If the expression contains a hunt-group, the matching contents of that group
  * is reported to the user. For example: If you specify "ERROR: (.*)" here, if
  * the server response contains the line "ERROR: md5sum check failed", the
  * string "md5sum check failed" is used for the exception message. </td>
  * </tr>
  * <tr>
  * <td>stringUploadSuccess</td>
  * <td>empty string ("") since 2.9.0<br>
  * (was ".* 200 OK$" before) <br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>This string is a regular expression. It allows the applet to test that
  * the server has accepted the upload. If this parameter is given to the applet,
  * the upload thread will try to match this regular epression to each lines
  * returned from the server.<br>
  * If the match is successfull once, the upload is considered to be a success.
  * If not, a {@link wjhk.jupload2.exception.JUploadExceptionUploadFailed} is
  * thrown. <br>
  * The default test expression testes that the web server returns no HTTP error:
  * 200 is the return code for a successfull HTTP request. It actually means that
  * postURL is a valid URL, and that the applet was able to send a request to
  * this URL: there should be no problem with the network configuration, like
  * proxy, password proxy...). <br>
  * <b>But</b> it doesn't mean that the uploaded files have correctly be managed
  * by the server. For instance, the URL can be http://sourceforge.net, which, of
  * course, would not take your files into account. <br>
  * So, as soon as you know a regular expression that test the return from the
  * target application (and not just a techical HTTP response code), change the
  * stringUploadSuccess to this value. For instance, the
  * {@link wjhk.jupload2.policies.CoppermineUploadPolicy} changes this value to
  * "^SUCCESS$", as the HTTP body content of the server's answer contain just
  * this exact line. This 'success' means that the pictures have correctly be
  * added to the album, that vignettes have been generated (this I suppose),
  * etc... </td>
  * </tr>
  * <tr>
  * <td>targetPictureFormat</td>
  * <td><i>Empty String</i> <br>
  * <br> (<b>to be</b> implemented in
  * {@link wjhk.jupload2.policies.PictureUploadPolicy})</td>
  * <td>This parameter can contain any picture writer known by the JVM. For
  * instance: jpeg, png, gif. All standard formats should be available. More
  * information can be found on the <a
  * href="http://java.sun.com/j2se/1.4.2/docs/guide/imageio/spec/title.fm.html">java.sun.com</a>
  * web site. </td>
  * </tr>
  * <tr>
  * <td><b>uploadPolicy</b></td>
  * <td>DefaultUploadPolicy <br>
  * <br>
  * see {@link wjhk.jupload2.policies.UploadPolicyFactory}</td>
  * <td>This parameter contains the class name for the UploadPolicy that should
  * be used. If it is not set, or if its value is unknown from
  * {@link wjhk.jupload2.policies.UploadPolicyFactory#getUploadPolicy(JUploadApplet)},
  * the {@link wjhk.jupload2.policies.DefaultUploadPolicy} is used. </td>
  * </tr>
  * <tr>
  * <td>urlToSendErrorTo</td>
  * <td><i>null</i> <br>
  * <br>
  * {@link wjhk.jupload2.policies.DefaultUploadPolicy}</td>
  * <td>If this url is given, and an upload error occurs, the applet posts all
  * debug output to this address. It's up to this URL to handle this message. It
  * is possible to just store the file, or to log the error in a database, or to
  * send a mail (like the mail.php script given with the coppermine pack). <br>
  * <u>Note:</u> Only http and https URL schemes are supported.</td>
  * </tr>
  * </table>
  * <h3><a name="example">HTML call example</a></h3>
  * <p>
  * Below, an example of how to put the applet into a PHP page is shown:
  * </p>
  * <code><pre>
  *   &lt;applet name=&quot;JUpload&quot; code=&quot;wjhk.jupload2.JUploadApplet&quot;
  *     archive=&quot;plugins/jupload/wjhk.jupload.jar&quot;
  *     &lt;!-- Applet display size, on the navigator page --&gt;
  *     width=&quot;500&quot; height=&quot;700&quot;
  *     &lt;!-- The applet uses some javascript functions, so we must allow that : --&gt;
  *     mayscript&gt;
  *     &lt;!-- No parameter is mandatory. We don't precise the UploadPolicy, so
  *          DefaultUploadPolicy is used. The applet behaves like the original
  *          JUpload. (jupload v1) --&gt;
  *     &lt;param name=&quot;postURL&quot; value=&quot;http://some.host.com/youruploadpage.php&quot;&gt;
  *     Java 1.5 or higher plugin required.
  *   &lt;/applet&gt;
  * </pre></code> <!-- ANT_COPYDOC_END --> <!-- ATTENTION: The previous comment is used
  * by Ant build. DO NOT CHANGE!! -->
  * 
  * @author Etienne Gauthier
  * @version $Revision$
  * @see wjhk.jupload2.policies.DefaultUploadPolicy
  */
 
 public interface UploadPolicy {
 
     /*
      * Available parameters for the applet. New parameters (for instance for new
      * policies) should all be added here, in alphabetic order. This ensures
      * that all tags are unique
      */
 
     /**
      * Parameter/Property name for URL to be loaded after an successful upload.
      */
     final static String PROP_AFTER_UPLOAD_URL = "afterUploadURL";
 
     /**
      * Parameter/Property name for allowing persistent HTTP connections.
      */
     final static String PROP_ALLOW_HTTP_PERSISTENT = "allowHttpPersistent";
 
     /**
      * Parameter/Property name for specifying the allowed file extensions
      */
     final static String PROP_ALLOWED_FILE_EXTENSIONS = "allowedFileExtensions";
 
     /**
      * Parameter/Property name for specifying the album id
      */
     final static String PROP_ALBUM_ID = "albumId";
 
     /**
      * Parameter/Property name for specifying if images should be cached in
      * memory. Be careful: if set to true, you'll probably have memory problems
      * while in a navigator.
      */
     final static String PROP_STORE_BUFFERED_IMAGE = "storeBufferedImage";
 
     /**
      * Parameter/Property name for specifying the debug level
      */
     final static String PROP_DEBUG_LEVEL = "debugLevel";
 
     /**
      * Parameter/Property name for specifying the UI language
      */
     final static String PROP_LANG = "lang";
 
     /**
      * Parameter/Property name for specifying the encoding of file names.
      */
     final static String PROP_FILENAME_ENCODING = "filenameEncoding";
 
     /**
      * Parameter/Property name for specifying additional form data.
      */
     final static String PROP_FORMDATA = "formdata";
 
     /**
      * Parameter/Property name for specifying high quality previews.
      */
     final static String PROP_HIGH_QUALITY_PREVIEW = "highQualityPreview";
 
     /**
      * Parameter/Property name for specifying a PLAF class to load.
      */
     final static String PROP_LOOK_AND_FEEL = "lookAndFeel";
 
     /**
      * Parameter/Property name for specifying the maximum size of a chunk of
      * uploaded data.
      */
     final static String PROP_MAX_CHUNK_SIZE = "maxChunkSize";
 
     /**
      * Parameter/Property name for specifying the maximum size of a single file.
      */
     final static String PROP_MAX_FILE_SIZE = "maxFileSize";
 
     /**
      * Parameter/Property name for specifying the maximum height of a picture.
      */
     final static String PROP_MAX_HEIGHT = "maxPicHeight";
 
     /**
      * Parameter/Property name for specifying the maximum width of a picture.
      */
     final static String PROP_MAX_WIDTH = "maxPicWidth";
 
     /**
      * Parameter/Property name for specifying the maximum number of file to be
      * uploaded in a single request.
      */
     final static String PROP_NB_FILES_PER_REQUEST = "nbFilesPerRequest";
 
     /**
      * Parameter/Property name for specifying URL of the upload post request.
      */
     final static String PROP_POST_URL = "postURL";
 
     /**
      * Parameter/Property name for specifying URL of the upload post request.
      */
     final static String PROP_AFTER_UPLOAD_TARGET = "afterUploadTarget";
 
     /**
      * Parameter/Property name for specifying the real (server-side-desired)
      * picture height.
      */
     final static String PROP_REAL_MAX_HEIGHT = "realMaxPicHeight";
 
     /**
      * Parameter/Property name for specifying the real (server-side-desired)
      * picture width.
      */
     final static String PROP_REAL_MAX_WIDTH = "realMaxPicWidth";
 
     /**
      * Parameter/Property name for specifying the server protocol version.
      */
     final static String PROP_SERVER_PROTOCOL = "serverProtocol";
 
     /**
      * Parameter/Property name for specifying if the log window should be
      * visible.
      */
     final static String PROP_SHOW_LOGWINDOW = "showLogWindow";
 
     /**
      * Parameter/Property name for specifying if the status bar should be
      * visible.
      */
     final static String PROP_SHOW_STATUSBAR = "showStatusbar";
 
     /**
      * Parameter/Property name for specifying how certificates are handled when
      * uploading via SSL.
      */
     final static String PROP_SSL_VERIFY_CERT = "sslVerifyCert";
 
     /**
      * Parameter/Property name for specifying if the pattern that indicates an
      * error in the server's response-body.
      */
     final static String PROP_STRING_UPLOAD_ERROR = "stringUploadError";
 
     /**
      * Parameter/Property name for specifying if the pattern that indicates
      * success in the server's response-body.
      */
     final static String PROP_STRING_UPLOAD_SUCCESS = "stringUploadSuccess";
 
     /**
      * Parameter/Property name for specifying the target picture format.
      */
     final static String PROP_TARGET_PICTURE_FORMAT = "targetPictureFormat";
 
     /**
      * Parameter/Property name for specifying the upload policy class.
      */
     final static String PROP_UPLOAD_POLICY = "uploadPolicy";
 
     /**
      * Parameter/Property name for specifying the URL for delivering error
      * reports.
      */
     final static String PROP_URL_TO_SEND_ERROR_TO = "urlToSendErrorTo";
 
     /**
      * Default value for parameter "afterUploadTarget".
      */
     final static String DEFAULT_AFTER_UPLOAD_TARGET = null;
 
     /**
      * Default value for parameter "afterUploadURL"
      */
     final static String DEFAULT_AFTER_UPLOAD_URL = null;
 
     /**
      * Default value for parameter "allowHttpPersisten".
      */
     final static boolean DEFAULT_ALLOW_HTTP_PERSISTENT = true;
 
     /**
      * Default value for parameter "allowedFileExtensions".
      */
     final static String DEFAULT_ALLOWED_FILE_EXTENSIONS = "";
 
     /**
      * Default value for parameter "albumId".
      */
     final static int DEFAULT_ALBUM_ID = 0;
 
     /**
      * Default value for parameter "storeBufferedImage". Be careful: if set to
      * true, you'll probably have memory problems while in a navigator.
      */
     final static boolean DEFAULT_STORE_BUFFERED_IMAGE = false;
 
     /**
      * Default value for parameter "debugLevel".
      */
     final static int DEFAULT_DEBUG_LEVEL = 0;
 
     /**
      * Default value for parameter "lang".
      */
     final static String DEFAULT_LANG = null;
 
     /**
      * Default value for parameter "filenameEncoding". Note: the
      * CoppermineUploadPolicy forces it to "UTF8".
      */
     final static String DEFAULT_FILENAME_ENCODING = null;
 
     /**
      * Default value for parameter "highQualityPreview".
      */
     final static boolean DEFAULT_HIGH_QUALITY_PREVIEW = false;
 
     /**
      * Default value for parameter "lookAndFeel".
      */
     final static String DEFAULT_LOOK_AND_FEEL = "";
 
     /**
      * Default value for parameter "maxChunkSize".
      */
     final static long DEFAULT_MAX_CHUNK_SIZE = Long.MAX_VALUE;
 
     /**
      * Default value for parameter "maxFileSize". Take care of this parameter if
      * chunk upload is activated! See comment above.
      */
     final static long DEFAULT_MAX_FILE_SIZE = Long.MAX_VALUE;
 
     /**
      * Default value for parameter "maxPicWidth".
      */
     final static int DEFAULT_MAX_WIDTH = -1;
 
     /**
      * Default value for parameter "maxPicHeight".
      */
     final static int DEFAULT_MAX_HEIGHT = -1;
 
     /**
      * Default value for parameter "maxPicHeight". Note: the
      * CoppermineUploadPolicy forces it to 1.
      */
     final static int DEFAULT_NB_FILES_PER_REQUEST = -1;
 
     /**
      * Default value for parameter "postURL".
      */
     final static String DEFAULT_POST_URL = null;
 
     /**
      * Default value for parameter "realMaxPicWidth".
      */
     final static int DEFAULT_REAL_MAX_WIDTH = -1;
 
     /**
      * Default value for parameter "realMaxPicHeight".
      */
     final static int DEFAULT_REAL_MAX_HEIGHT = -1;
 
     /**
      * Default value for parameter "serverProtocol".
      */
     final static String DEFAULT_SERVER_PROTOCOL = null;
 
     /**
      * Default value for parameter "showLogWindow".
      */
     final static boolean DEFAULT_SHOW_LOGWINDOW = true;
 
     /**
      * Default value for parameter "showStatusBar".
      */
     final static boolean DEFAULT_SHOW_STATUSBAR = true;
 
     /**
      * Default value for parameter "sslVerifyCert"
      */
     final static String DEFAULT_SSL_VERIFY_CERT = "none";
 
     /**
      * Default value for parameter "stringUploadError".
      * 
      * @since 2.9.2rc4
      */
     final static String DEFAULT_STRING_UPLOAD_ERROR = "";
 
     /**
      * Default value for parameter "stringUploadSuccess". Note: was ".* 200 OK$"
      * before 2.9.0
      */
     final static String DEFAULT_STRING_UPLOAD_SUCCESS = "";
 
     /**
      * Default value for parameter "targetPictureFormat".
      */
     final static String DEFAULT_TARGET_PICTURE_FORMAT = null;
 
     /**
      * Default value for parameter "uploadPolicy".
      */
     final static String DEFAULT_UPLOAD_POLICY = "DefaultUploadPolicy";
 
     /**
      * Default value for parameter "urlToSendErrorTo".
      */
     final static String DEFAULT_URL_TO_SEND_ERROR_TO = null;
 
     /**
      * Default value for parameter "formdata"
      * 
      * @since 2.9.2rc4
      */
     final static String DEFAULT_FORMDATA = null;
 
     /**
      * This method is called to create the top panel. The default implementation
      * is defined in
      * {@link wjhk.jupload2.policies.DefaultUploadPolicy#createTopPanel(JButton, JButton, JButton, JPanel)}.
      * 
      * @param browse The default browse button.
      * @param remove The default removeSelected button.
      * @param removeAll The default removeAll button.
      * @param mainPanel The panel that contains all objects. It can be used to
      *            change the cursor (to a WAIT_CURSOR for instance).
      * @return the topPanel, that will be displayed on the top of the Applet.
      */
     public JPanel createTopPanel(JButton browse, JButton remove,
             JButton removeAll, JPanel mainPanel);
 
     /**
      * This method is called to create the progress panel. The default
      * implementation is defined in
      * {@link wjhk.jupload2.policies.DefaultUploadPolicy#createProgressPanel(JProgressBar, JButton, JButton, JPanel)}.
      * 
      * @param progressBar The default progress bar.
      * @param uploadButton The default upload button.
      * @param stopButton The default stop button.
      * @param mainPanel The panel that contains all objects. It can be used to
      *            change the cursor (to a WAIT_CURSOR for instance).
      * @return the topPanel, that will be displayed on the top of the Applet.
      */
     public JPanel createProgressPanel(JProgressBar progressBar,
             JButton uploadButton, JButton stopButton, JPanel mainPanel);
 
     /**
      * This method is used to create a new status bar. The default
      * implementation is defined in
      * {@link wjhk.jupload2.policies.DefaultUploadPolicy#createStatusBar(JLabel, JPanel)}.
      * 
      * @param statusContent The status bar content
      * @param mainPanel The panel that contains all objects. It can be used to
      *            change the cursor (to a WAIT_CURSOR for instance).
      * @return the topPanel, that will be displayed on the top of the Applet.
      */
     public JPanel createStatusBar(JLabel statusContent, JPanel mainPanel);
 
     /**
      * This methods creates a new FileData instance (or one of its inherited
      * classes), and return it to the caller.
      * 
      * @param file The file used to create the FileData instance. This method is
      *            called once for each file selected by the user, even if the
      *            user added several files in one 'shot'.
      * @return A FileData instance. The exact class depends on the
      *         currentUploadPolicy. Can be null, if the policy performs checks,
      *         and the given file is not Ok for these controls. See
      *         {@link PictureUploadPolicy#createFileData(File)} for an example.
      *         It's up to the upload policy to display a message to inform the
      *         user that this file won't be added to the file list.
      */
     public FileData createFileData(File file);
 
     /**
      * This method displays the applet parameter list, according to the current
      * debugLevel. It is called by the {@link #setDebugLevel(int)} method. It
      * should be override by any subclasses, that should display its own
      * parameters, then call <i>super.displayParameterStatus()</i>.
      */
     public void displayParameterStatus();
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// getters / setters
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * This allow runtime modifications of properties. With this method, you can
      * change any applet parameter after the applet initilization, with
      * JavaScript for instance. If the applet parameters given in <i>prop</i>
      * is not managed by this method, a warning is displayed in the log window.
      * 
      * @param prop The applet parameter name.
      * @param value The new value for this parameter. If the value is not valid
      *            (for instance <i>aaa</i> for a number), a warning is
      *            displayed in the log window, and the existing value is not
      *            changed.
      */
     public void setProperty(String prop, String value) throws JUploadException;
 
     /**
      * Retrieves the current value for the afterUploadURL applet parameter.
      * 
      * @return The current value for he afterUploadURL applet parameter.
      */
     public String getAfterUploadURL();
 
     /**
      * Retrieves the current value for allowHttpPersistent
      * 
      * @return Current value for allowHttpPersistent
      */
     public boolean getAllowHttpPersistent();
 
     /**
      * Retrieves the current value for allowedFileExtensions *
      * 
      * @return Current value for allowedFileExtensions
      */
     public String getAllowedFileExtensions();
 
     /**
      * A useful function, that has nothing to do with an upload policy. But it
      * is useful to have it here, as the uploadPolicy is known everywhere in the
      * applet.
      * 
      * @return Reference to the applet.
      */
     public JUploadApplet getApplet();
 
     /**
      * This method indicate whether or not the debug messages must be displayed.
      * Default is no debug (0). <br>
      * To activate the debug, add a 'debugLevel' parameter to the applet (with 1
      * to n value), or call this method. Currently, level used in the code are
      * between 0 (no debug) and 100 (max debug). <br>
      * With a 0 value, no debug messages will be displayed. The
      * {@link DefaultUploadPolicy}.addMsgToDebugBufferString method stores all
      * debug output in a BufferString.
      * 
      * @param debugLevel The new debugLevel.
      * @see DefaultUploadPolicy#sendDebugInformation(String)
      */
     public void setDebugLevel(int debugLevel);
 
     /**
      * This method returns the current debug level.
      * 
      * @return The current debug level
      * @see #setDebugLevel(int)
      */
     public int getDebugLevel();
 
     /**
      * Return the encoding that should be used for the filename. This encoding
      * has no impact on the content of the file that will be uploaded.
      * 
      * @return The encoding name, like UTF-8 (see the Charset JDK
      *         documentation).
      */
     public String getFilenameEncoding();
 
     /**
      * Returns the value of the applet parameter maxChunkSize (see above for a
      * description of all applet parameters)
      * 
      * @return the current value of maxChunkSize.
      */
     public long getMaxChunkSize();
 
     /**
      * Returns the value of the applet parameter maxFileSize (see above for a
      * description of all applet parameters)
      * 
      * @return the current value of maxFileSize.
      */
     public long getMaxFileSize();
 
     /**
      * This function returns the number of files should be uploaded during one
      * access to the server. If negative or 0, all files are to be uploaded in
      * one HTTP request. If positive, each HTTP upload contains this number of
      * files. The last upload request may contain less files. <br>
      * Examples :
      * <UL>
      * <li>If 1 : files are uploaded file by file.
      * <li>If 5 : files are uploaded 5 files by 5 files. If 12 files are
      * uploaded, 3 HTTP upload are done, containing 5, 5 and 2 files.
      * </UL>
      * 
      * @return Returns the maximum number of files, to download in one HTTP
      *         request.
      */
     public int getNbFilesPerRequest();
 
     /**
      * Get the target URL for upload.
      * 
      * @return Should be valid URL...
      */
     public String getPostURL();
 
     /**
      * The URL can change during the life of our policy ...
      * 
      * @param postURL
      */
     public void setPostURL(String postURL) throws JUploadException;
 
     /**
      * Return the target, specified as applet parameter "afterUploadTarget"
      * 
      * @return the specified target.
      */
     public String getAfterUploadTarget();
 
     /**
      * HTTP protocol that should be used to send the HTTP request. Currently,
      * this is mainly used by
      * {@link wjhk.jupload2.policies.CoppermineUploadPolicy}, as the coppermine
      * control that the protocol used for each HTTP request is the same as the
      * one used during the session creation. It is used in the default policy,
      * as it could be used elsewhere. <br>
      * Default is : HTTP/1.1
      * 
      * @return The selected server protocol.
      */
     public String getServerProtocol();
 
     /**
      * Retrieves SSL verification mode.
      * 
      * @return The current SSL verification mode.
      */
     public int getSslVerifyCert();
 
     /**
      * Indicate whether the log window should be shown. It may be convenient to
      * hide it, as it contains mostly debug information. But it still is the
      * only place where possible errors and warnings are shown.<br>
      * Default is : true
      * 
      * @return The current value for the <i>showStatusBar</i> applet parameter.
      */
     public boolean getShowLogWindow();
 
     /**
      * Get the original name of the file on the disk. This function can encode
      * the filename (see the filenameEncoding parameter). By default, the
      * original filename is returned.
      * 
      * @param fileData
      * @param index
      * @return The filename the is given in the filename part of the
      *         Content-Disposition header.
      */
     public String getUploadFilename(FileData fileData, int index)
             throws JUploadException;
 
     /**
      * Get an upload filename, that is to be send in the HTTP upload request.
      * This is the name part of the Content-Disposition header. That is: this is
      * the name under which you can manage the file (for instance in the
      * _FILES[$name] in PHP) and not the filename of the original file.
      * 
      * @param index index of the file within upload (can be4, ou of 10 for
      *            instance).
      * @return The name part of the Content-Disposition header.
      * @see #getUploadFilename(FileData, int)
      */
     public String getUploadName(FileData fileData, int index);
 
     /**
      * Returns the current URL where error log must be posted. See <a
      * href="#parameters">Parameters</a>
      * 
      * @return the urlToSendErrorTo
      */
     public String getUrlToSendErrorTo();
 
     /**
      * Retrieve the regular expression that will be tested against each line of
      * the server answer. If one line matches this expression, that upload is
      * marked as failed. <br>
      * 
      * @return The regular expression that must be run again each line of the
      *         http answer.
      */
     public String getStringUploadError();
 
     /**
      * Get the regular expression that will be tested against each line of the
      * server answer. If one line matches this expression, that upload is marked
      * as successful. <br>
      * The upload works this way:
      * <ol>
      * <li>Upload the selected file(s) to the server
      * <li>Get all the server HTTP response.
      * <li>The stringUploadSuccess regular expression is tested against each
      * line from the server.
      * <li>If the above test gives a match, the upload is marked as successful.
      * Else, the upload is marked as unsuccessful, and a
      * JUploadExceptionUploadFailure is thrown.
      * </ol>
      * 
      * @return The regular expression that must be run again each line of the
      *         http answer.
      */
     public String getStringUploadSuccess();
 
     /**
      * Retrieve the applet's "formdata" parameter.
      * 
      * @return The applet's formdata parameter.
      */
     public String getFormdata();
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// miscellanneous methods
     // ////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * This methods is called by the {@link JUploadFileFilter#accept(File)}. It
      * allows the current upload policy to filter files, according to any
      * choosen applet behaviour.<br>
      * In the {@link DefaultUploadPolicy} upload policy, this filter is based on
      * the applet parameter: <i>allowedFileExtensions</i>.
      * 
      * @see JUploadPanel#JUploadPanel(java.awt.Container,
      *      wjhk.jupload2.gui.JUploadTextArea, UploadPolicy)
      */
     public boolean fileFilterAccept(File file);
 
     /**
      * Return a description for the FileFilter, according to the current upload
      * policy.
      */
     public String fileFilterGetDescription();
 
     /**
      * Response for the {@link JUploadFileView#getIcon(File)}. Default is
      * implemented in {@link DefaultUploadPolicy#fileViewGetIcon(File)}, by
      * returning null, which displays the default icon.
      * 
      * @param file The file from which the icon should represent.
      * @return The resulting icon.
      */
     public Icon fileViewGetIcon(File file);
 
     /**
      * This method allows the applet to post debug information to the website
      * (see {@link #getUrlToSendErrorTo()}). Then, it is possible to log the
      * error, to send a mail...
      * 
      * @param reason A string describing briefly the problem. The mail subject
      *            will be something like: Jupload Error (reason)
      */
     public void sendDebugInformation(String reason);
 
     /**
      * log an error message, based on an exception. Will be logged in the log
      * window, if defined.
      * 
      * @param e The exception to report
      */
     public void displayErr(Exception e);
 
     /**
      * log an error message. Will be logged in the log window, if defined.
      * 
      * @param err The erreur message to be displayed.
      */
     public void displayErr(String err);
 
     /**
      * log an error message. Will be logged in the log window, if defined.
      * 
      * @param err The error message to be displayed.
      * @param e An exception. It's stacktrace is logged.
      */
     public void displayErr(String err, Exception e);
 
     /**
      * log an info message. Will be logged in the log window, if defined.
      * 
      * @param info The information message that will be displayed.
      */
     public void displayInfo(String info);
 
     /**
      * log a warning message. Will be logged in the log window, if defined.
      * 
      * @param warn The warning message that will be displayed.
      */
     public void displayWarn(String warn);
 
     /**
      * log a debug message. Will be logged in the log window, if defined.
      * 
      * @param debug The message to display.
      * @param minDebugLevel If the current debug level is superior or equals to
      *            minDebugLevel, the message will be displayed. Otherwise, it
      *            will be ignored.
      */
     public void displayDebug(String debug, int minDebugLevel);
 
     /**
      * Add an header to the list of headers that will be added to each HTTP
      * upload request. This method is called from specific uploadPolicies, which
      * would need headers to be added to all uploads. These headers are used in
      * {@link wjhk.jupload2.policies.DefaultUploadPolicy}.
      * 
      * @param header
      * @see #onAppendHeader(StringBuffer)
      */
     public void addHeader(String header);
 
     /**
      * Append specific headers for this upload (session cookies, for instance).
      * This method is called while building each upload HTTP request.
      * 
      * @param sb The header StringBuffer where sp�cific headers should be
      *            appended.
      * @return The StringBuffer given in parameters. This is conform to the
      *         StringBuffer.append method.
      * @see #addHeader(String)
      * @see wjhk.jupload2.upload.FileUploadThread
      */
     public StringBuffer onAppendHeader(StringBuffer sb);
 
     /**
      * This method is called each time a file is selected in the panel files. It
      * allows, for instance, to preview a picture
      * {@link wjhk.jupload2.policies.PictureUploadPolicy}.
      * 
      * @param fileData
      */
     public void onSelectFile(FileData fileData);
 
     /**
      * Indicate if everything is ready for upload.
      * 
      * @return indicate if everything is ready for upload.
      */
     public boolean isUploadReady();
 
     /**
      * Enable any action, required before an upload. For instance,
      * {@link PictureUploadPolicy} disable the rotation buttons during buttons.
      * 
      * @see #afterUpload(Exception, String)
      */
     public void beforeUpload();
 
     /**
      * This method returns true, if upload is a success. A HTTP response of "200
      * OK" indicates that the server response is techically correct. But, it may
      * be a functionnal error. For instance, the server could answer by a proper
      * HTTP page, that the user is no allowed to upload files. It's up to the
      * uploadPolicy to check this, and answer true or false to this method. <br>
      * This method is called once for each HTTP request toward the server. For
      * instance, if the upload is done file by file, and there are three files
      * to upload, this method will be called three times. <br>
      * So this method is different from the
      * {@link #afterUpload(Exception, String)}, that will be called only once
      * in this case, after the three calls to the checkUploadSuccess method.
      * 
      * @param status The numeric response status (e.g. 200)
      * @param msg The status message from the first line of the response (e.g.
      *            "200 OK").
      * @param body The http body part (that is: the serverOuput without the http
      *            headers and the blank line that follow them).
      * @return true (or an exception is raised, instead of returning false).
      *         This garantees that all cases are handled: the compiler will
      *         indicate an error if the code can come to the end of the method,
      *         without finding a 'return' or a throw exception. This return code
      *         can be ignored by the caller.
      */
     public boolean checkUploadSuccess(int status, String msg, String body)
             throws JUploadException;
 
     /**
      * This method is called after an upload, whether it is successful or not.
      * This method is called once for each click of the user on the 'upload'
      * button. That is: if the nbFilesPerRequest is 2, and the user selected 5
      * files before clicking on the 'upload' button. Then the afterUpload is
      * called once the 5 files were uploaded to the server.
      * 
      * @param e null if success, or the exception indicating the problem.
      * @param serverOutput The full server output, including the HTTP headers.
      */
     public void afterUpload(Exception e, String serverOutput);
 
     /**
      * Retrieve a local property. This allows localization. All strings are
      * stored in the property files in the wjhk.jupload2.lang package.
      * 
      * @param key The key, whose associated text is to retrieve.
      * @return The associated text.
      * @see wjhk.jupload2.policies.DefaultUploadPolicy#DefaultUploadPolicy(JUploadApplet)
      */
     public String getString(String key);
 
     /**
      * Retrive a local property. This allows localization. All strings are
      * stored in the property files in the wjhk.jupload2.lang package. <br>
      * All occurences of <b>{1}</b> in the value (corresponding to key) are
      * replaced by value1. <br>
      * Sample : <br>
      * Love=Oh {1}, I love you so much ... <br>
      * Call it by <i>getString("Love", "John Smith")</i> ... &nbsp; ;-)
      * 
      * @param key The key, whose associated text is to retrieve.
      * @param value1 The value, which will replace all occurence of {1}
      * @return The associated text.
      * @see wjhk.jupload2.policies.DefaultUploadPolicy#DefaultUploadPolicy(JUploadApplet)
      */
     public String getString(String key, String value1);
 
     /**
      * Same as {@link #getString(String, String)}, for two parameters.
      * 
      * @param key The key, whose associated text is to retrieve.
      * @param value1 The first value, which will replace all occurence of {1}
      * @param value2 The second value, which will replace all occurence of {2}
      * @return The associated text.
      * @see wjhk.jupload2.policies.DefaultUploadPolicy#DefaultUploadPolicy(JUploadApplet)
      */
     public String getString(String key, String value1, String value2);
 
     /**
      * Same as {@link #getString(String, String)}, for three parameters.
      * 
      * @param key The key, whose associated text is to retrieve.
      * @param value1 The first value, which will replace all occurence of {1}
      * @param value2 The second value, which will replace all occurence of {2}
      * @param value3 The third value, which will replace all occurence of {3}
      * @return The associated text.
      * @see wjhk.jupload2.policies.DefaultUploadPolicy#DefaultUploadPolicy(JUploadApplet)
      */
     public String getString(String key, String value1, String value2,
             String value3);
 
     /**
      * Same as {@link #getString(String,String)}, but the given value is an
      * integer.
      * 
      * @param key The key, whose associated text is to retrieve.
      * @param value1 The value, which will replace all occurence of {1}
      * @return The associated text.
      */
     public String getString(String key, int value1);
 
     /**
      * alert displays a MessageBox with a unique 'Ok' button, like the
      * javascript alert function.
      * 
      * @param key The string identifying the text to display, depending on the
      *            current language.
      * @see #alertStr(String)
      */
     public void alert(String key);
 
     /**
      * alert displays a MessageBox with a unique 'Ok' button, like the
      * javascript alert function.
      * 
      * @param str The full String that must be displayed to the user.
      * @see #alert(String)
      */
     void alertStr(String str);
 
     /**
      * alert displays a MessageBox with a unique 'Ok' button, like the
      * javascript alert function.
      * 
      * @param key The string identifying the text to display, depending on the
      *            current language.
      * @param arg1 A string that will replace all {1} in the text corresponding
      *            to property_str. This allows to have dynamic localized text.
      */
     public void alert(String key, String arg1);
 }
