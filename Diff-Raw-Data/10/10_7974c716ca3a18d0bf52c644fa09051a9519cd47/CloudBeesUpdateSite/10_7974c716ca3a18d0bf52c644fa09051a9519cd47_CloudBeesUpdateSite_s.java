 /*
  * The MIT License
  *
  * Copyright (c) 2011-2012, CloudBees, Inc., Stephen Connolly.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.cloudbees.jenkins.plugins.freeplugins;
 
 import com.trilead.ssh2.crypto.Base64;
 import hudson.model.Hudson;
 import hudson.model.UpdateSite;
 import hudson.util.FormValidation;
 import hudson.util.TextFile;
 import jenkins.model.Jenkins;
 import net.sf.json.JSONObject;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.output.NullOutputStream;
 import org.apache.commons.io.output.TeeOutputStream;
 import org.jvnet.hudson.crypto.CertificateUtil;
 import org.jvnet.hudson.crypto.SignatureOutputStream;
 import org.kohsuke.stapler.StaplerRequest;
 
 import javax.servlet.ServletContext;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectStreamException;
 import java.io.OutputStreamWriter;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.security.DigestOutputStream;
 import java.security.GeneralSecurityException;
 import java.security.MessageDigest;
 import java.security.Signature;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateExpiredException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.CertificateNotYetValidException;
 import java.security.cert.TrustAnchor;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import static hudson.util.TimeUnit2.DAYS;
 
 /**
  * An update site that uses the CloudBees certificate for signing.
  */
 public class CloudBeesUpdateSite extends UpdateSite {
     /**
      * Our logger
      */
     private static final Logger LOGGER = Logger.getLogger(CloudBeesUpdateSite.class.getName());
 
     private static final long DAY = DAYS.toMillis(1);
 
     /**
      * The last timestamp of the data. Mirrors {@link UpdateSite#dataTimestamp}.
      */
     private transient long dataTimestamp = -1;
 
     private transient volatile long lastAttempt = -1;
 
     /**
      * Constructor.
      *
      * @param id  the ID of the update site.
      * @param url the url of the update site.
      */
     public CloudBeesUpdateSite(String id, String url) {
         super(id, url);
     }
 
     /**
      * Called when object has been deserialized from a stream.
      *
      * @return {@code this}, or a replacement for {@code this}.
      * @throws java.io.ObjectStreamException if the object cannot be restored.
      * @see <a href="http://download.oracle.com/javase/1.3/docs/guide/serialization/spec/input.doc6.html">The Java
      *      Object Serialization Specification</a>
      */
     private Object readResolve() throws ObjectStreamException {
         setDataTimestamp(-1);
         return this;
     }
 
     /**
      * returns the data timestamp.
      *
      * @return
      */
     public long getDataTimestamp() {
         return dataTimestamp;
     }
 
     @Override
     public String getDownloadUrl() {
         /*
             HACKISH:
 
             Loading scripts in HTTP from HTTPS pages cause browsers to issue a warning dialog.
             The elegant way to solve the problem is to always load update center from HTTPS,
             but our backend mirroring scheme isn't ready for that. So this hack serves regular
             traffic in HTTP server, and only use HTTPS update center for Jenkins in HTTPS.
 
             We'll monitor the traffic to see if we can sustain this added traffic.
          */
         String url = getUrl();
         if (url.startsWith("http://jenkins-updates.cloudbees.com/") && Jenkins.getInstance().isRootUrlSecure())
             return "https"+url.substring(4);
         return url;
     }
 
 
     /**
      * Sets the data timestamp (and tries to propagate the change to {@link UpdateSite#dataTimestamp}
      *
      * @param dataTimestamp the new data timestamp.
      */
     private void setDataTimestamp(long dataTimestamp) {
         try {
             // try reflection to be safe for the parent class changing the location
             Field field = UpdateSite.class.getDeclaredField("dataTimestamp");
             boolean accessible = field.isAccessible();
             try {
                 field.setLong(this, dataTimestamp);
             } finally {
                 if (!accessible) {
                     field.setAccessible(false);
                 }
             }
         } catch (Throwable e) {
             // ignore
         }
         this.dataTimestamp = dataTimestamp;
     }
 
     /**
      * When was the last time we asked a browser to check the data for us?
      * Mirrors {@link hudson.model.UpdateSite#dataTimestamp}.
      */
     private long getLastAttempt() {
         return lastAttempt;
     }
 
     private void setLastAttempt(long lastAttempt) {
         try {
             // try reflection to be safe for the parent class changing the location
             Field field = UpdateSite.class.getDeclaredField("lastAttempt");
             boolean accessible = field.isAccessible();
             try {
                 field.setLong(this, lastAttempt);
             } finally {
                 if (!accessible) {
                     field.setAccessible(false);
                 }
             }
         } catch (Throwable e) {
             // ignore
         }
         this.lastAttempt = lastAttempt;
     }
 
     /**
      * This is where we store the update center data.
      * Mirrors {@link hudson.model.UpdateSite#getDataFile()}
      */
     private TextFile getDataFile() {
         try {
             // try reflection to be safe for the parent class changing the location
             Method method = UpdateSite.class.getDeclaredMethod("getDataFile");
             boolean accessible = method.isAccessible();
             try {
                 method.setAccessible(true);
                 return (TextFile) method.invoke(this);
             } finally {
                 if (!accessible) {
                     method.setAccessible(false);
                 }
             }
         } catch (Throwable e) {
             // ignore
         }
         return new TextFile(new File(Hudson.getInstance().getRootDir(), "updates/" + getId() + ".json"));
     }
 
     /**
      * This is the endpoint that receives the update center data file from the browser.
      * Mirrors {@link UpdateSite#doPostBack(org.kohsuke.stapler.StaplerRequest)} as there is no other way to override
      * the verification of the signature.
      */
     @Override
     public FormValidation doPostBack(StaplerRequest req) throws IOException, GeneralSecurityException {
         setDataTimestamp(System.currentTimeMillis());
         String json = hudson.util.IOUtils.toString(req.getInputStream(), "UTF-8");
         JSONObject o = JSONObject.fromObject(json);
 
         int v = o.getInt("updateCenterVersion");
         if (v != 1) {
             throw new IllegalArgumentException("Unrecognized update center version: " + v);
         }
 
         if (signatureCheck) {
             FormValidation e = verifySignature(o);
             if (e.kind != FormValidation.Kind.OK) {
                 LOGGER.severe(e.renderHtml());
                 return e;
             }
         }
 
         LOGGER.info("Obtained the latest update center data file for UpdateSource " + getId());
         getDataFile().write(json);
         return FormValidation.ok();
     }
 
     @Override
     public FormValidation doVerifySignature() throws IOException {
         JSONObject jsonObject = getJSONObject();
         if (getId().equals(jsonObject.optString("id"))) {
             return verifySignature(jsonObject);
         } else {
             return super.doVerifySignature();
         }
     }
 
     /**
      * Verifies the signature in the update center data file.
      */
     private FormValidation verifySignature(JSONObject o) throws IOException {
         try {
             FormValidation warning = null;
 
             JSONObject signature = o.getJSONObject("signature");
             if (signature.isNullObject()) {
                 return FormValidation.error("No signature block found in update center '" + getId() + "'");
             }
             o.remove("signature");
 
             List<X509Certificate> certs = new ArrayList<X509Certificate>();
             {// load and verify certificates
                 CertificateFactory cf = CertificateFactory.getInstance("X509");
                 for (Object cert : signature.getJSONArray("certificates")) {
                     X509Certificate c = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                             Base64.decode(cert.toString().toCharArray())));
                     try {
                         c.checkValidity();
                     } catch (CertificateExpiredException e) { // even if the certificate isn't valid yet,
                         // we'll proceed it anyway
                         warning = FormValidation.warning(e,
                                 String.format("Certificate %s has expired in update center '%s'", cert.toString(),
                                         getId()));
                     } catch (CertificateNotYetValidException e) {
                         warning = FormValidation.warning(e,
                                 String.format("Certificate %s is not yet valid in update center '%s'", cert.toString(),
                                         getId()));
                     }
                     certs.add(c);
                 }
 
                 // all default root CAs in JVM are trusted, plus certs bundled in Jenkins
                 Set<TrustAnchor> anchors = new HashSet<TrustAnchor>(); // CertificateUtil.getDefaultRootCAs();
                 ServletContext context = Hudson.getInstance().servletContext;
                 anchors.add(new TrustAnchor(loadLicenseCaCertificate(), null));
                 for (String cert : (Set<String>) context.getResourcePaths("/WEB-INF/update-center-rootCAs")) {
                     if (cert.endsWith(".txt")) {
                         continue;       // skip text files that are meant to be documentation
                     }
                     InputStream stream = context.getResourceAsStream(cert);
                     if (stream != null) {
                         try {
                             anchors.add(new TrustAnchor((X509Certificate) cf.generateCertificate(stream), null));
                         } finally {
                             IOUtils.closeQuietly(stream);
                         }
                     }
                 }
                 CertificateUtil.validatePath(certs, anchors);
             }
 
             // this is for computing a digest to check sanity
             MessageDigest sha1 = MessageDigest.getInstance("SHA1");
             DigestOutputStream dos = new DigestOutputStream(new NullOutputStream(), sha1);
 
             // this is for computing a signature
             Signature sig = Signature.getInstance("SHA1withRSA");
             sig.initVerify(certs.get(0));
             SignatureOutputStream sos = new SignatureOutputStream(sig);
 
             // until JENKINS-11110 fix, UC used to serve invalid digest (and therefore unverifiable signature)
             // that only covers the earlier portion of the file. This was caused by the lack of close() call
             // in the canonical writing, which apparently leave some bytes somewhere that's not flushed to
             // the digest output stream. This affects Jenkins [1.424,1,431].
             // Jenkins 1.432 shipped with the "fix" (1eb0c64abb3794edce29cbb1de50c93fa03a8229) that made it
             // compute the correct digest, but it breaks all the existing UC json metadata out there. We then
             // quickly discovered ourselves in the catch-22 situation. If we generate UC with the correct signature,
             // it'll cut off [1.424,1.431] from the UC. But if we don't, we'll cut off [1.432,*).
             //
             // In 1.433, we revisited 1eb0c64abb3794edce29cbb1de50c93fa03a8229 so that the original "digest"/"signature"
             // pair continues to be generated in a buggy form, while "correct_digest"/"correct_signature" are generated
             // correctly.
             //
             // Jenkins should ignore "digest"/"signature" pair. Accepting it creates a vulnerability that allows
             // the attacker to inject a fragment at the end of the json.
             o.writeCanonical(new OutputStreamWriter(new TeeOutputStream(dos, sos), "UTF-8")).close();
 
             // did the digest match? this is not a part of the signature validation, but if we have a bug in the c14n
             // (which is more likely than someone tampering with update center), we can tell
             String computedDigest = new String(Base64.encode(sha1.digest()));
             String providedDigest = signature.optString("correct_digest");
             if (providedDigest == null) {
                 return FormValidation.error("No correct_digest parameter in update center '" + getId()
                         + "'. This metadata appears to be old.");
             }
             if (!computedDigest.equalsIgnoreCase(providedDigest)) {
                 return FormValidation
                         .error("Digest mismatch: " + computedDigest + " vs " + providedDigest + " in update center '"
                                 + getId() + "'");
             }
 
             String providedSignature = signature.getString("correct_signature");
             if (!sig.verify(Base64.decode(providedSignature.toCharArray()))) {
                 return FormValidation
                         .error("Signature in the update center doesn't match with the certificate in update center '"
                                 + getId() + "'");
             }
 
             if (warning != null) {
                 return warning;
             }
             return FormValidation.ok();
         } catch (GeneralSecurityException e) {
             return FormValidation.error(e, "Signature verification failed in the update center '" + getId() + "'");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isDue() {
         if (neverUpdate) {
             return false;
         }
         if (getDataTimestamp() == -1) {
             setDataTimestamp(getDataFile().file.lastModified());
         }
         long now = System.currentTimeMillis();
         boolean due = now - getDataTimestamp() > DAY && now - getLastAttempt() > 15000;
         if (due) {
             setLastAttempt(now);
         }
         return due;
     }
 
     /*package*/
     static X509Certificate loadLicenseCaCertificate() throws CertificateException {
         CertificateFactory cf = CertificateFactory.getInstance("X.509");
         InputStream stream = CloudBeesUpdateSite.class.getResourceAsStream("/cloudbees-root-cacert.pem");
         try {
             return stream != null ? (X509Certificate) cf.generateCertificate(stream) : null;
         } finally {
             IOUtils.closeQuietly(stream);
         }
     }
 
 }
