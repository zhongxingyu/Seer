 /*
  * Copyright 2013 ENERKO Informatik GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
  * AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
  * FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
  * DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
  * SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package de.enerko.reports2;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 
 import org.junit.Test;
 
 /**
  * @author Michael J. Simons, 2013-06-18
  */
 public class ReportEngineTest extends AbstractDatabaseTest {	
 	@Test
 	public void shouldHandleValidStatements() throws IOException {
 		final ReportEngine reportEngine = new ReportEngine(connection);
 		
 		final Report report = reportEngine.createReportFromStatement("Select 's1' as sheetname, 1 as cell_column, 1 as cell_row, 'c1' as cell_name, 'string' as cell_type, 'cv' as cell_value from dual");
 		
 		File outFile = File.createTempFile(ReportEngineTest.class.getSimpleName() + "-", ".xls");
 		report.write(new BufferedOutputStream(new FileOutputStream(outFile)));
 		
 		ReportEngine.logger.log(Level.INFO, String.format("Report written to %s", outFile.getAbsolutePath()));
 	}
 	
 	@Test
 	public void shouldHandleValidFunctions() throws IOException {
 		final ReportEngine reportEngine = new ReportEngine(connection);
 		
 		final Report report = reportEngine.createReport("pck_enerko_reports2_test.f_fb_report_source_test", "5", "21.09.1979", "test");
 		
 		File outFile = File.createTempFile(ReportEngineTest.class.getSimpleName() + "-", ".xls");		
 		report.write(new BufferedOutputStream(new FileOutputStream(outFile)));
 		
 		ReportEngine.logger.log(Level.INFO, String.format("Report written to %s", outFile.getAbsolutePath()));
 	}
 	
 	@Test
	public void shouldHandleTemplates() throws IOException {
 		final ReportEngine reportEngine = new ReportEngine(connection);
 		
 		final InputStream template = this.getClass().getResource("/template1.xls").openStream();
 				
 		final Report report = reportEngine.createReport("pck_enerko_reports2_test.f_fb_report_source_test", template, "5", "21.09.1979", "test");
 		
 		File outFile = File.createTempFile(ReportEngineTest.class.getSimpleName() + "-", ".xls");		
 		report.write(new BufferedOutputStream(new FileOutputStream(outFile)));
 		
 		ReportEngine.logger.log(Level.INFO, String.format("Report written to %s", outFile.getAbsolutePath()));		
 	}
 	
 	@Test
 	public void displayAllFeatures() throws IOException {
 		final ReportEngine reportEngine = new ReportEngine(connection);
 				
 		final Report report = reportEngine.createReport("pck_enerko_reports2_test.f_all_features", this.getClass().getResource("/template2.xls").openStream());
 		
 		File outFile = File.createTempFile(ReportEngineTest.class.getSimpleName() + "-", ".xls");		
 		report.write(new BufferedOutputStream(new FileOutputStream(outFile)));
 		
 		ReportEngine.logger.log(Level.INFO, String.format("Report written to %s", outFile.getAbsolutePath()));		
 	}
 }
