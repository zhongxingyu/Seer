 /**
  * Copyright (c) 2011 Red Hat, Inc.
  *
  * This software is licensed to you under the GNU General Public License,
  * version 2 (GPLv2). There is NO WARRANTY for this software, express or
  * implied, including the implied warranties of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
  * along with this software; if not, see
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
  *
  * Red Hat trademarks are not licensed under GPLv2. No permission is
  * granted to use or replicate Red Hat trademarks that are incorporated
  * in this software or its documentation.
  */
 package org.candlepin.thumbslug.ssl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertTrue;
 
 
 /**
  * CertParserTest
  */
 public class CertParserTest {
 
     @Test
     public void parseV1Cert() throws Exception {
         InputStream is =
             this.getClass().getResourceAsStream("/certs/v1-cert.pem");
         String pem = convertStreamToString(is);
         CertParser parser = new CertParser(pem);
 
         assertTrue(parser.getCert().trim().startsWith("-----BEGIN CERTIFICATE-----"));
         assertTrue(parser.getCert().trim().endsWith("-----END CERTIFICATE-----"));
 
         assertTrue(parser.getKey().trim().startsWith("-----BEGIN RSA PRIVATE KEY-----"));
         assertTrue(parser.getKey().trim().endsWith("-----END RSA PRIVATE KEY-----"));
     }
 
     @Test
     public void parseV3Cert() throws Exception {
         InputStream is =
             this.getClass().getResourceAsStream("/certs/v3-cert.pem");
         String pem = convertStreamToString(is);
         CertParser parser = new CertParser(pem);
 
         assertTrue(parser.getCert().trim().startsWith("-----BEGIN CERTIFICATE-----"));
         assertTrue(parser.getCert().trim().endsWith("-----END CERTIFICATE-----"));
 
         assertTrue(parser.getKey().trim().startsWith("-----BEGIN RSA PRIVATE KEY-----"));
         assertTrue(parser.getKey().trim().endsWith("-----END RSA PRIVATE KEY-----"));
     }
 
     @Test
     public void parseV1CertKeyFirst() throws Exception {
         InputStream is =
             this.getClass().getResourceAsStream("/certs/v1-cert-reversed.pem");
         String pem = convertStreamToString(is);
         CertParser parser = new CertParser(pem);
 
         assertTrue(parser.getCert().trim().startsWith("-----BEGIN CERTIFICATE-----"));
         assertTrue(parser.getCert().trim().endsWith("-----END CERTIFICATE-----"));
 
         assertTrue(parser.getKey().trim().startsWith("-----BEGIN RSA PRIVATE KEY-----"));
         assertTrue(parser.getKey().trim().endsWith("-----END RSA PRIVATE KEY-----"));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void parseV3CertNoKey() throws Exception {
         InputStream is =
             this.getClass().getResourceAsStream("/certs/v3-cert-no-key.pem");
         String pem = convertStreamToString(is);
         CertParser parser = new CertParser(pem);
 
         assertTrue(parser.getCert().trim().startsWith("-----BEGIN CERTIFICATE-----"));
         assertTrue(parser.getCert().trim().endsWith("-----END CERTIFICATE-----"));
 
         assertTrue(parser.getKey().trim().startsWith("-----BEGIN RSA PRIVATE KEY-----"));
         assertTrue(parser.getKey().trim().endsWith("-----END RSA PRIVATE KEY-----"));
     }
 
     public String convertStreamToString(InputStream is) throws IOException {
         Writer writer = new StringWriter();
 
         char[] buffer = new char[1024];
         try {
             Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             int n;
             while ((n = reader.read(buffer)) != -1) {
                 writer.write(buffer, 0, n);
             }
         }
         finally {
             is.close();
         }
         return writer.toString();
     }
}
