 /**
  * --------------------------------------------------------------------------
  *                   OpenMS -- Open-Source Mass Spectrometry
  * --------------------------------------------------------------------------
  * Copyright The OpenMS Team -- Eberhard Karls University Tuebingen,
  * ETH Zurich, and Freie Universitaet Berlin 2002-2014.
  * 
  * This software is released under a three-clause BSD license:
  *  * Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of any author or any participating institution
  *    may be used to endorse or promote products derived from this software
  *    without specific prior written permission.
  * For a full list of authors, refer to the file AUTHORS.
  * --------------------------------------------------------------------------
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL ANY OF THE AUTHORS OR THE CONTRIBUTING
  * INSTITUTIONS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package de.openms.knime.startupcheck;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.knime.core.node.NodeLogger;
 import org.knime.workbench.ui.startup.StartupMessage;
 import org.knime.workbench.ui.startup.StartupMessageProvider;
 
 import de.openms.knime.startupcheck.registryaccess.WinRegistryQuery;
 
 /**
  * @author aiche
  * 
  */
 public class OpenMSStartupMessageProvider implements StartupMessageProvider {
 
 	private static final NodeLogger LOGGER = NodeLogger
 			.getLogger(OpenMSStartupMessageProvider.class);
 	
 	private static final String OPENMS_REQUIREMENTS_URI = "http://sourceforge.net/projects/open-ms/files/OpenMS/OpenMS-1.10/OpenMS-1.10-win32-prerequisites-installer.exe/download";
 
 	private static final String REG_DWORD_1 = "0x1";
 	private static final String VCREDIST_X64_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Microsoft\\VisualStudio\\10.0\\VC\\VCRedist\\x64";
	private static final String VCREDIST_X86_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Microsoft\\VisualStudio\\10.0\\VC\\VCRedist\\x86";
 	private static final String NET35_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v3.5";
 	private static final String NET4_FULL_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full";
 	private static final String NET4_CLIENT_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Client";
 
 	@Override
 	public List<StartupMessage> getMessages() {
 		try {
 			if (isWindows()) {
 
 				boolean dotNet4ValueClientExists = WinRegistryQuery.checkDWord(
 						NET4_CLIENT_KEY, "Install", REG_DWORD_1);
 				LOGGER.debug(".NET4 Client Value exists: "
 						+ dotNet4ValueClientExists);
 
 				boolean dotNet4ValueFullExists = WinRegistryQuery.checkDWord(
 						NET4_FULL_KEY, "Install", REG_DWORD_1);
 				LOGGER.debug(".NET4 Full Value exists: "
 						+ dotNet4ValueFullExists);
 
 				boolean dotNet35ValueExists = WinRegistryQuery.checkDWord(
 						NET35_KEY, "Install", REG_DWORD_1);
 				LOGGER.debug(".NET3.5 1031 Value exists: "
 						+ dotNet35ValueExists);
 
 				boolean vcRedist2010_x86ValueExists = WinRegistryQuery
 						.checkDWord(VCREDIST_X86_KEY, "Installed", REG_DWORD_1);
 				LOGGER.debug("VC10 x86 Redist Value exists: "
 						+ vcRedist2010_x86ValueExists);
 
 				if (!(vcRedist2010_x86ValueExists && dotNet35ValueExists
 						&& dotNet4ValueClientExists && dotNet4ValueFullExists)) {
 					return getWarning();
 				}
 
 				if (is64BitSystem()) {
 					boolean vcRedist2010_x64ValueExists = WinRegistryQuery
 							.checkDWord(VCREDIST_X64_KEY, "Installed",
 									REG_DWORD_1);
 					LOGGER.debug("VC10 x64 Redist Value exists: "
 							+ vcRedist2010_x64ValueExists);
 
 					if (!vcRedist2010_x64ValueExists) {
 						return getWarning();
 					}
 				}
 			}
 		} catch (IllegalArgumentException e) {
 			LOGGER.warn("Error when querying windows registry.", e);
 		}
 		return new ArrayList<StartupMessage>();
 	}
 
 	private List<StartupMessage> getWarning() {
 		final String longMessage = String
 				.format("Some of the requirements for the OpenMS KNIME Nodes are missing on your system. " +
 						"Please download the requirements installer from <a href=\"%s\">here</a>.",
 						OPENMS_REQUIREMENTS_URI);
 		final String shortMessage = "Some of the OpenMS KNIME Nodes requirements are missing. Double click for details.";
 
 		StartupMessage message = new StartupMessage(longMessage, shortMessage,
 				StartupMessage.WARNING, Activator.getInstance().getBundle());
 		List<StartupMessage> messages = new ArrayList<StartupMessage>();
 		messages.add(message);
 		return messages;
 	}
 
 	private boolean is64BitSystem() {
 		return "64".equals(System.getProperty("sun.arch.data.model"));
 	}
 
 	private boolean isWindows() {
 		return System.getProperty("os.name").startsWith("Windows");
 	}
 }
