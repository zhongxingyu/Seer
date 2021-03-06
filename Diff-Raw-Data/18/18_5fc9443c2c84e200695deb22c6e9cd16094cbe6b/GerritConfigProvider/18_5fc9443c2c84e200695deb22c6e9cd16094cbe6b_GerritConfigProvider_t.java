 // Copyright (C) 2009 The Android Open Source Project
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package com.google.gerrit.server.config;
 
 import com.google.gerrit.client.data.ApprovalType;
 import com.google.gerrit.client.data.GerritConfig;
 import com.google.gerrit.client.data.GitwebLink;
 import com.google.gerrit.client.reviewdb.ApprovalCategory;
 import com.google.gerrit.client.reviewdb.Project;
 import com.google.gerrit.client.reviewdb.ReviewDb;
 import com.google.gerrit.client.rpc.Common;
 import com.google.gerrit.server.ContactStore;
 import com.google.gerrit.server.mail.EmailSender;
 import com.google.gerrit.server.ssh.SshInfo;
 import com.google.gwtorm.client.OrmException;
 import com.google.gwtorm.client.SchemaFactory;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.ProvisionException;
 
 import org.spearce.jgit.lib.Config;
 
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 
 public class GerritConfigProvider implements Provider<GerritConfig> {
   private static boolean isIPv6(final InetAddress ip) {
     return ip instanceof Inet6Address
         && ip.getHostName().equals(ip.getHostAddress());
   }
 
   private final Config cfg;
   private final String canonicalWebUrl;
   private final AuthConfig authConfig;
   private final SchemaFactory<ReviewDb> schema;
   private final Project.NameKey wildProject;
  private final SshInfo sshInfo;
 
   private EmailSender emailSender;
   private ContactStore contactStore;
 
   @Inject
   GerritConfigProvider(@GerritServerConfig final Config gsc,
       @CanonicalWebUrl @Nullable final String cwu, final AuthConfig ac,
       final SchemaFactory<ReviewDb> sf,
      @WildProjectName final Project.NameKey wp, final SshInfo si) {
     cfg = gsc;
     canonicalWebUrl = cwu;
     authConfig = ac;
     schema = sf;
    sshInfo = si;
     wildProject = wp;
   }
 
   @Inject(optional = true)
   void setEmailSender(final EmailSender d) {
     emailSender = d;
   }
 
   @Inject(optional = true)
   void setContactStore(final ContactStore d) {
     contactStore = d;
   }
 
   private GerritConfig create() throws OrmException {
     final GerritConfig config = new GerritConfig();
     config.setCanonicalUrl(canonicalWebUrl);
     config.setUseContributorAgreements(cfg.getBoolean("auth",
         "contributoragreements", false));
     config.setGitDaemonUrl(cfg.getString("gerrit", null, "canonicalgiturl"));
     config.setUseRepoDownload(cfg.getBoolean("repo", null,
         "showdownloadcommand", false));
     config.setUseContactInfo(contactStore != null && contactStore.isEnabled());
     config.setAllowRegisterNewEmail(emailSender != null
         && emailSender.isEnabled());
     config.setLoginType(authConfig.getLoginType());
     config.setWildProject(wildProject);
 
     final String gitwebUrl = cfg.getString("gitweb", null, "url");
     if (gitwebUrl != null) {
       config.setGitwebLink(new GitwebLink(gitwebUrl));
     }
 
     final ReviewDb db = schema.open();
     try {
       for (final ApprovalCategory c : db.approvalCategories().all()) {
         config.add(new ApprovalType(c, db.approvalCategoryValues().byCategory(
             c.getId()).toList()));
       }
     } finally {
       db.close();
     }
 
    final InetSocketAddress addr =
        sshInfo != null ? sshInfo.getAddress() : null;
     if (addr != null) {
       final InetAddress ip = addr.getAddress();
       String host;
       if (ip != null && ip.isAnyLocalAddress()) {
         host = "";
       } else if (isIPv6(ip)) {
         host = "[" + addr.getHostName() + "]";
       } else {
         host = addr.getHostName();
       }
       if (addr.getPort() != 22) {
         host += ":" + addr.getPort();
       }
       config.setSshdAddress(host);
     }
 
     Common.setGerritConfig(config);
     return config;
   }
 
   @Override
   public GerritConfig get() {
     try {
       return create();
     } catch (OrmException e) {
       throw new ProvisionException("Cannot construct GerritConfig", e);
     }
   }
 }
