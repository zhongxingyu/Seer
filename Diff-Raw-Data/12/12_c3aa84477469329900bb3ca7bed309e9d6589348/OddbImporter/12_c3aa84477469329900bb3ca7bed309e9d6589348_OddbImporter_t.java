 /*******************************************************************************
  * Copyright (c) 2006-2010, G. Weirich and Elexis
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Niklaus Giger - initial implementation
  *******************************************************************************/
 package ch.ngiger.elexis.oddb_ch.data;
 
 import java.io.File;
 import java.util.Date;
 import java.util.List;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.oddb.ch.Import;
 import ch.elexis.data.Artikel;
 import ch.elexis.data.PersistentObject;
 import ch.elexis.data.Query;
 import ch.elexis.util.ImporterPage;
 import ch.elexis.util.SWTHelper;
 import ch.rgw.tools.ExHandler;
 import ch.ngiger.elexis.oddb_ch.Messages;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class OddbImporter extends ImporterPage {
 	boolean bDelete = false;
 	Button bClear;
 	String mode;
 	public static final Logger logger = LoggerFactory.getLogger(ImporterPage.class);
 	
 	public OddbImporter(){}
 	
 	@Override
 	public String getTitle(){
 		return OddbArtikel.ODDB_NAME; //$NON-NLS-1$
 	}
 	
 	@Override
 	public String getDescription(){
 		return Messages.OddbImporter_PleaseSelectFile;
 	}
 	
 	static final String EQUALS = "="; //$NON-NLS-1$
 	static final String ODDB = "ODDB"; //$NON-NLS-1$
 	
 	@Override
 	public IStatus doImport(final IProgressMonitor monitor) throws Exception{
 		mode = Messages.OddbImporter_ModeUpdateAdd;
 		if (bDelete == true) {
 			PersistentObject.getConnection().exec("DELETE FROM ARTIKEL WHERE TYP='ODDB'"); //$NON-NLS-1$
 			mode = Messages.OddbImporter_ModeCreateNew;
 		}
 		String mainTask = "ODDB-Import of " + results[0];
 		monitor.beginTask(mainTask, 0);
 		int cachetime = PersistentObject.getDefaultCacheLifetime();
 		PersistentObject.setDefaultCacheLifetime(2);
 		Import oddbImport = new Import();
 		logger.info(String.format("Mode %1$s: Starting import of %2$s", mode, results[0]));
 		File file = new File(results[0]);
 		logger.info(String.format("Size is %d kB", file.length() / 1024));
 		monitor.subTask(String.format(
 			"Oddb - Read YAML %1$s (%2$d kBytes)", file.getCanonicalPath(), file.length() / 1024)); //$NON-NLS-1$
 		boolean res = oddbImport.importFile(file.getAbsolutePath());
 		int nrArticles = oddbImport.articles.size();
 		int j = 0;
 		Query<Artikel> qbe = new Query<Artikel>(Artikel.class);
 		if (res) {
 			try {
 				String msg = String.format("ODDB convert %1$d articles", nrArticles);//$NON-NLS-1$
 				monitor.beginTask(mainTask, nrArticles);
 				monitor.subTask(msg);
 				logger.info(msg);
 				Date d1 = new Date(System.currentTimeMillis());
 				for (j = 0; j < nrArticles; j++) {
 					Import.ElexisArtikel a = oddbImport.articles.get(j);
 					if (j % 10 == 0)
 						logger.info("j: " + j + ":" + a.toString());
 					Artikel oddbA = null;
 					qbe.clear();
 					qbe.add("EAN", EQUALS, a.ean13);
 					qbe.and();
 					qbe.add("Typ", EQUALS, ODDB);
 					List<Artikel> lArt = qbe.execute();
 					if (lArt.size() == 1) {
 						oddbA = lArt.get(0);
 					} else if (lArt.size() == 0) {
 						oddbA = new Artikel(a.name, ODDB, a.pharmacode);
 					} else {
 						// TODO: handle duplicates
 						logger.error(String.format("ODDB-Duplikat ?? %1$s", a.ean13));
 					}
 					if (a.EKPreis != null)
 						oddbA.setEKPreis(a.EKPreis);
 					if (a.VKPreis != null)
 						oddbA.setVKPreis(a.VKPreis);
 					if (a.atc_code != null)
 						oddbA.setATC_code(a.atc_code);
 					if (a.ean13 != null)
 						oddbA.setEAN(a.ean13);
 					if (a.ean13 != null)
 						oddbA.setEAN(a.ean13);
 					if (a.pharmacode != null)
 						oddbA.setPharmaCode(a.pharmacode);
 					if (a.atc_code != null)
 						oddbA.setATC_code(a.atc_code);
 					if (a.verpackungsEinheit != null)
 						oddbA.setExt(Artikel.VERPACKUNGSEINHEIT, a.verpackungsEinheit);
 					if (a.abgabeEinheit != null)
 						oddbA.setExt(Artikel.VERKAUFSEINHEIT, a.abgabeEinheit);
 					monitor.worked(1);
 					if (j % 1000 == 500) { // Speicher freigeben
 						PersistentObject.clearCache();
 						System.gc();
 						Thread.sleep(100);
 					}
 				}
 				Date d2 = new Date(System.currentTimeMillis());
 				long difference = d2.getTime() - d1.getTime();
 				logger.info(String.format("Elapsed %1$d.%2$d seconds. Converted %3$d articles",
 					difference / 1000, difference % 1000, nrArticles));
 				
 			} catch (Exception ex) {
 				logger.error("Error converting articles: " + ex.getMessage()
 					+ ex.getStackTrace().toString());
 				ExHandler.handle(ex);
 			}
 			PersistentObject.setDefaultCacheLifetime(cachetime);
 			monitor.done();
 			return Status.OK_STATUS;
 		}
 		PersistentObject.setDefaultCacheLifetime(cachetime);
 		monitor.done();
 		return Status.CANCEL_STATUS;
 	}
 	
 	@Override
 	public void collect(){
 		bDelete = bClear.getSelection();
 	}
 	
 	@Override
 	public Composite createPage(final Composite parent){
 		Composite ret = new ImporterPage.FileBasedImporter(parent, this);
 		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
 		bClear = new Button(parent, SWT.CHECK | SWT.WRAP);
 		bClear.setText(Messages.OddbImporter_ClearAllData);
 		bClear.setSelection(true);
 		bClear.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
 		return ret;
 		
 	}
 	
 }
