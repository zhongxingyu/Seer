 package com.iver.cit.gvsig;
 
 import java.awt.Component;
 import java.awt.geom.Point2D;
 import java.io.File;
 import java.io.IOException;
 import java.sql.Types;
 import java.util.Vector;
 
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.ProgressMonitor;
 
 import org.cresques.cts.ICoordTrans;
 
 import com.hardcode.driverManager.Driver;
 import com.hardcode.driverManager.DriverLoadException;
 import com.hardcode.gdbms.driver.exceptions.FileNotFoundDriverException;
 import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
 import com.hardcode.gdbms.driver.exceptions.OpenDriverException;
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.data.driver.DriverException;
 import com.hardcode.gdbms.engine.values.Value;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.andami.plugins.Extension;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
 import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
 import com.iver.cit.gvsig.fmap.MapContext;
 import com.iver.cit.gvsig.fmap.core.DefaultFeature;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.ShapeFactory;
 import com.iver.cit.gvsig.fmap.core.v02.FLabel;
 import com.iver.cit.gvsig.fmap.drivers.ConnectionFactory;
 import com.iver.cit.gvsig.fmap.drivers.DBException;
 import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
 import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
 import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
 import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
 import com.iver.cit.gvsig.fmap.drivers.IConnection;
 import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
 import com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver;
 import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
 import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
 import com.iver.cit.gvsig.fmap.drivers.dxf.DXFMemoryDriver;
 import com.iver.cit.gvsig.fmap.drivers.jdbc.postgis.PostGISWriter;
 import com.iver.cit.gvsig.fmap.drivers.jdbc.postgis.PostGisDriver;
 import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
 import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.edition.IWriter;
 import com.iver.cit.gvsig.fmap.edition.writers.dxf.DxfFieldsMapping;
 import com.iver.cit.gvsig.fmap.edition.writers.dxf.DxfWriter;
 import com.iver.cit.gvsig.fmap.edition.writers.shp.ShpWriter;
 import com.iver.cit.gvsig.fmap.layers.FBitSet;
 import com.iver.cit.gvsig.fmap.layers.FLayer;
 import com.iver.cit.gvsig.fmap.layers.FLayers;
 import com.iver.cit.gvsig.fmap.layers.FLyrAnnotation;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.layers.LayerFactory;
 import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
 import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
 import com.iver.cit.gvsig.project.documents.view.IProjectView;
 import com.iver.cit.gvsig.project.documents.view.gui.View;
 import com.iver.cit.gvsig.vectorialdb.ConnectionSettings;
 import com.iver.cit.gvsig.vectorialdb.DlgConnection;
 import com.iver.utiles.PostProcessSupport;
 import com.iver.utiles.SimpleFileFilter;
 import com.iver.utiles.swing.threads.AbstractMonitorableTask;
 
 public class ExportTo extends Extension {
 	private String lastPath = null;
 	private class WriterTask extends AbstractMonitorableTask
 	{
 		FLyrVect lyrVect;
 		IWriter writer;
 		int rowCount;
 		ReadableVectorial va;
 		SelectableDataSource sds;
 		FBitSet bitSet;
 		MapContext mapContext;
 		VectorialDriver reader;
 
 		public WriterTask(MapContext mapContext, FLyrVect lyr, IWriter writer, Driver reader) throws ReadDriverException
 		{
 			this.mapContext = mapContext;
 			this.lyrVect = lyr;
 			this.writer = writer;
 			this.reader = (VectorialDriver) reader;
 
 			setInitialStep(0);
 			setDeterminatedProcess(true);
 			setStatusMessage(PluginServices.getText(this, "exportando_features"));
 
 			va = lyrVect.getSource();
 			sds = lyrVect.getRecordset();
 
 			bitSet = sds.getSelection();
 
 			if (bitSet.cardinality() == 0)
 				rowCount = va.getShapeCount();
 			else
 				rowCount = bitSet.cardinality();
 
 			setFinalStep(rowCount);
 
 		}
 		public void run() throws Exception {
 			va.start();
 			ICoordTrans ct = lyrVect.getCoordTrans();
 			DriverAttributes attr = va.getDriverAttributes();
 			boolean bMustClone = false;
 			if (attr != null) {
 				if (attr.isLoadedInMemory()) {
 					bMustClone = attr.isLoadedInMemory();
 				}
 			}
 			if (lyrVect instanceof FLyrAnnotation && lyrVect.getShapeType()!=FShape.POINT) {
 				SHPLayerDefinition lyrDef=(SHPLayerDefinition)writer.getTableDefinition();
 				lyrDef.setShapeType(FShape.POINT);
 				writer.initialize(lyrDef);
 			}
 
 			// Creamos la tabla.
 			writer.preProcess();
 
 			if (bitSet.cardinality() == 0) {
 				rowCount = va.getShapeCount();
 				for (int i = 0; i < rowCount; i++) {
 					IGeometry geom = va.getShape(i);
 					if (geom == null) {
 						continue;
 					}
 					if (lyrVect instanceof FLyrAnnotation && geom.getGeometryType()!=FShape.POINT) {
 						Point2D p=FLabel.createLabelPoint((FShape)geom.getInternalShape());
 						geom=ShapeFactory.createPoint2D(p.getX(),p.getY());
 					}
 					if (ct != null) {
 						if (bMustClone)
 							geom = geom.cloneGeometry();
 						geom.reProject(ct);
 					}
 					reportStep();
 					setNote(PluginServices.getText(this, "exporting_") + i);
 					if (isCanceled())
 						break;
 
 					if (geom != null) {
 						Value[] values = sds.getRow(i);
 						IFeature feat = new DefaultFeature(geom, values, "" + i);
 						DefaultRowEdited edRow = new DefaultRowEdited(feat,
 								DefaultRowEdited.STATUS_ADDED, i);
 						writer.process(edRow);
 					}
 				}
 			} else {
 				int counter = 0;
 				for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet
 						.nextSetBit(i + 1)) {
 					IGeometry geom = va.getShape(i);
 					if (geom == null) {
 						continue;
 					}
 					if (lyrVect instanceof FLyrAnnotation && geom.getGeometryType()!=FShape.POINT) {
 						Point2D p=FLabel.createLabelPoint((FShape)geom.getInternalShape());
 						geom=ShapeFactory.createPoint2D(p.getX(),p.getY());
 					}
 					if (ct != null) {
 						if (bMustClone)
 							geom = geom.cloneGeometry();
 						geom.reProject(ct);
 					}
 					reportStep();
 					setNote(PluginServices.getText(this, "exporting_") + counter);
 					if (isCanceled())
 						break;
 
 					if (geom != null) {
 						Value[] values = sds.getRow(i);
 						IFeature feat = new DefaultFeature(geom, values, "" + i);
 						DefaultRowEdited edRow = new DefaultRowEdited(feat,
 								DefaultRowEdited.STATUS_ADDED, i);
 
 						writer.process(edRow);
 					}
 				}
 
 			}
 
 			writer.postProcess();
 			va.stop();
 			if (reader != null){
 				int res = JOptionPane.showConfirmDialog(
 					(JComponent) PluginServices.getMDIManager().getActiveWindow()
 					, PluginServices.getText(this, "insertar_en_la_vista_la_capa_creada"),
 					PluginServices.getText(this,"insertar_capa"),
 					JOptionPane.YES_NO_OPTION);
 
 				if (res == JOptionPane.YES_OPTION)
 				{
 					PostProcessSupport.executeCalls();
 					ILayerDefinition lyrDef = (ILayerDefinition) writer.getTableDefinition();
 					FLayer newLayer = LayerFactory.createLayer(
 							lyrDef.getName(), reader, mapContext.getProjection());
 					mapContext.getLayers().addLayer(newLayer);
 				}
 			}
 
 		}
 		/* (non-Javadoc)
 		 * @see com.iver.utiles.swing.threads.IMonitorableTask#finished()
 		 */
 		public void finished() {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 	private class MultiWriterTask extends AbstractMonitorableTask{
 		Vector tasks=new Vector();
 		public void addTask(WriterTask wt) {
 			tasks.add(wt);
 		}
 		public void run() throws Exception {
 			for (int i = 0; i < tasks.size(); i++) {
 				((WriterTask)tasks.get(i)).run();
 			}
 			tasks.clear();
 		}
 		/* (non-Javadoc)
 		 * @see com.iver.utiles.swing.threads.IMonitorableTask#finished()
 		 */
 		public void finished() {
 			// TODO Auto-generated method stub
 
 		}
 
 
 	}
 	/**
 	 * @see com.iver.andami.plugins.IExtension#initialize()
 	 */
 	public void initialize() {
 	}
 
 	/**
 	 * @see com.iver.andami.plugins.IExtension#execute(java.lang.String)
 	 */
 	public void execute(String actionCommand) {
 		com.iver.andami.ui.mdiManager.IWindow f = PluginServices.getMDIManager()
 				.getActiveWindow();
 
 		if (f instanceof View) {
 			View vista = (View) f;
 			IProjectView model = vista.getModel();
 			MapContext mapa = model.getMapContext();
 			FLayers layers = mapa.getLayers();
 			FLayer[] actives = layers.getActives();
 			try {
 				// NOTA: SI HAY UNA SELECCIN, SOLO SE SALVAN LOS SELECCIONADOS
 				for (int i = 0; i < actives.length; i++) {
 					if (actives[i] instanceof FLyrVect) {
 						FLyrVect lv = (FLyrVect) actives[i];
 						int numSelec = lv.getRecordset().getSelection()
 								.cardinality();
 						if (numSelec > 0) {
 							int resp = JOptionPane.showConfirmDialog(
 									(Component) PluginServices.getMainFrame(),
 									PluginServices.getText(this,"se_van_a_guardar_") + numSelec
 											+ PluginServices.getText(this,"features_desea_continuar"),
 									PluginServices.getText(this,"export_to"), JOptionPane.YES_NO_OPTION);
 							if (resp != JOptionPane.YES_OPTION) {
 								continue;
 							}
 						} // if numSelec > 0
 						if (actionCommand.equals("SHP")) {
 							saveToShp(mapa, lv);
 						}
 						if (actionCommand.equals("DXF")) {
 							saveToDxf(mapa, lv);
 						}
 						if (actionCommand.equals("POSTGIS")) {
 							saveToPostGIS(mapa, lv);
 						}
 					} // actives[i]
 				} // for
 			} catch (ReadDriverException e) {
 				NotificationManager.showMessageError(e.getMessage(),e);
 			}
 
 		}
 	}
 
 	public void saveToPostGIS(MapContext mapContext, FLyrVect layer){
 		try {
 			String tableName = JOptionPane.showInputDialog(PluginServices
 					.getText(this, "intro_tablename"));
 			if (tableName == null)
 				return;
 			tableName = tableName.toLowerCase();
 
 			DlgConnection dlg = new DlgConnection(new String[]{"PostGIS JDBC Driver"});
 			dlg.setModal(true);
 			dlg.setVisible(true);
 			ConnectionSettings cs = dlg.getConnSettings();
 			if (cs == null)
 				return;
 			IConnection conex = ConnectionFactory.createConnection(cs
 					.getConnectionString(), cs.getUser(), cs.getPassw());
 
 
 			DBLayerDefinition originalDef = null;
 			if (layer.getSource().getDriver() instanceof IVectorialDatabaseDriver) {
 				originalDef=((IVectorialDatabaseDriver) layer.getSource().getDriver()).getLyrDef();
 			}
 
 
 			DBLayerDefinition dbLayerDef = new DBLayerDefinition();
 			// Fjp:
 			// Cambio: En Postgis, el nombre de catlogo est siempre vaco. Es algo heredado de Oracle, que no se usa.
 			// dbLayerDef.setCatalogName(cs.getDb());
 			dbLayerDef.setCatalogName("");
 
 			// Aadimos el schema dentro del layer definition para poder tenerlo en cuenta.
 			dbLayerDef.setSchema(cs.getSchema());
 
 			dbLayerDef.setTableName(tableName);
 			dbLayerDef.setName(tableName);
 			dbLayerDef.setShapeType(layer.getShapeType());
 			SelectableDataSource sds = layer.getRecordset();
 
 			FieldDescription[] fieldsDescrip = sds.getFieldsDescription();
 			dbLayerDef.setFieldsDesc(fieldsDescrip);
 	        // Creamos el driver. OJO: Hay que aadir el campo ID a la
 	        // definicin de campos.
 
 			if (originalDef != null){
 				dbLayerDef.setFieldID(originalDef.getFieldID());
 				dbLayerDef.setFieldGeometry(originalDef.getFieldGeometry());
 
 			}else{
 				// Search for id field name
 				int index=0;
 				String fieldName="gid";
 				while (findFileByName(fieldsDescrip,fieldName) != -1){
 					index++;
 					fieldName="gid"+index;
 				}
 				dbLayerDef.setFieldID(fieldName);
 
 				// search for geom field name
 				index=0;
 				fieldName="the_geom";
 				while (findFileByName(fieldsDescrip,fieldName) != -1){
 					index++;
 					fieldName="the_geom"+index;
 				}
 				dbLayerDef.setFieldGeometry(fieldName);
 
 			}
 
 			// if id field dosen't exist we add it
 			if (findFileByName(fieldsDescrip,dbLayerDef.getFieldID()) == -1)
 			{
 	        	int numFieldsAnt = fieldsDescrip.length;
 	        	FieldDescription[] newFields = new FieldDescription[dbLayerDef.getFieldsDesc().length + 1];
 	            for (int i=0; i < numFieldsAnt; i++)
 	            {
 	            	newFields[i] = fieldsDescrip[i];
 	            }
 	            newFields[numFieldsAnt] = new FieldDescription();
 	            newFields[numFieldsAnt].setFieldDecimalCount(0);
 	            newFields[numFieldsAnt].setFieldType(Types.INTEGER);
 	            newFields[numFieldsAnt].setFieldLength(7);
 	            newFields[numFieldsAnt].setFieldName("gid");
 	            dbLayerDef.setFieldsDesc(newFields);
 
 	        }
 
 			// all fields to lowerCase
 			FieldDescription field;
 			for (int i=0;i<dbLayerDef.getFieldsDesc().length;i++){
 				field = dbLayerDef.getFieldsDesc()[i];
 				field.setFieldName(field.getFieldName().toLowerCase());
 			}
 			dbLayerDef.setFieldID(dbLayerDef.getFieldID().toLowerCase());
 			dbLayerDef.setFieldGeometry(dbLayerDef.getFieldGeometry().toLowerCase());
 
 			dbLayerDef.setWhereClause("");
 			String strSRID = layer.getProjection().getAbrev();
 			dbLayerDef.setSRID_EPSG(strSRID);
 			dbLayerDef.setConnection(conex);
 
 			PostGISWriter writer=(PostGISWriter)LayerFactory.getWM().getWriter("PostGIS Writer");
 			writer.setWriteAll(true);
 			writer.setCreateTable(true);
 			writer.initialize(dbLayerDef);
 			PostGisDriver postGISDriver=new PostGisDriver();
 			postGISDriver.setLyrDef(dbLayerDef);
 			postGISDriver.open();
 			PostProcessSupport.clearList();
 			Object[] params = new Object[2];
 			params[0] = conex;
 			params[1] = dbLayerDef;
 			PostProcessSupport.addToPostProcess(postGISDriver, "setData",
 					params, 1);
 
 			writeFeatures(mapContext, layer, writer, postGISDriver);
 
 		} catch (DriverLoadException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		} catch (DBException e) {
 			NotificationManager.showMessageError(e.getLocalizedMessage(),e);
 		} catch (InitializeWriterException e) {
			NotificationManager.addError(e.getMessage(),e);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		}
 
 	}
 
 	/**
 	 * Lanza un thread en background que escribe las features. Cuando termina, pregunta al usuario si quiere
 	 * aadir la nueva capa a la vista. Para eso necesita un driver de lectura ya configurado.
 	 * @param mapContext
 	 * @param layer
 	 * @param writer
 	 * @param reader
 	 * @throws ReadDriverException
 	 * @throws DriverException
 	 * @throws DriverIOException
 	 */
 	private void writeFeatures(MapContext mapContext, FLyrVect layer, IWriter writer, Driver reader) throws ReadDriverException
 	{
 		PluginServices.cancelableBackgroundExecution(new WriterTask(mapContext, layer, writer, reader));
 	}
 	private void writeMultiFeatures(MapContext mapContext, FLyrVect layers, IWriter[] writers, Driver[] readers) throws ReadDriverException{
 		MultiWriterTask mwt=new MultiWriterTask();
 		for (int i=0;i<writers.length;i++) {
 			mwt.addTask(new WriterTask(mapContext, layers, writers[i], readers[i]));
 		}
 		PluginServices.cancelableBackgroundExecution(mwt);
 	}
 	/**
 	 * @param layer
 	 *            FLyrVect to obtain features. If selection, only selected
 	 *            features will be precessed.
 	 * @param writer
 	 *            (Must be already initialized)
 	 * @throws ReadDriverException
 	 * @throws ProcessWriterException
 	 * @throws ExpansionFileReadException
 	 * @throws EditionException
 	 * @throws DriverException
 	 * @throws DriverIOException
 	 * @throws com.hardcode.gdbms.engine.data.driver.DriverException
 	 */
 	public void writeFeaturesNoThread(FLyrVect layer, IWriter writer) throws ReadDriverException, VisitorException, ExpansionFileReadException{
 		ReadableVectorial va = layer.getSource();
 		SelectableDataSource sds = layer.getRecordset();
 
 		// Creamos la tabla.
 		writer.preProcess();
 
 		int rowCount;
 		FBitSet bitSet = layer.getRecordset().getSelection();
 
 		if (bitSet.cardinality() == 0)
 			rowCount = va.getShapeCount();
 		else
 			rowCount = bitSet.cardinality();
 
 		ProgressMonitor progress = new ProgressMonitor(
 				(JComponent) PluginServices.getMDIManager().getActiveWindow(),
 				PluginServices.getText(this, "exportando_features"),
 				PluginServices.getText(this, "exportando_features"), 0,
 				rowCount);
 
 		progress.setMillisToDecideToPopup(200);
 		progress.setMillisToPopup(500);
 
 		if (bitSet.cardinality() == 0) {
 			rowCount = va.getShapeCount();
 			for (int i = 0; i < rowCount; i++) {
 				IGeometry geom = va.getShape(i);
 
 				progress.setProgress(i);
 				if (progress.isCanceled())
 					break;
 
 				if (geom != null) {
 					Value[] values = sds.getRow(i);
 					IFeature feat = new DefaultFeature(geom, values, "" + i);
 					DefaultRowEdited edRow = new DefaultRowEdited(feat,
 							IRowEdited.STATUS_ADDED, i);
 					writer.process(edRow);
 				}
 			}
 		} else {
 			int counter = 0;
 			for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet
 					.nextSetBit(i + 1)) {
 				IGeometry geom = va.getShape(i);
 
 				progress.setProgress(counter++);
 				if (progress.isCanceled())
 					break;
 
 				if (geom != null) {
 					Value[] values = sds.getRow(i);
 					IFeature feat = new DefaultFeature(geom, values, "" + i);
 					DefaultRowEdited edRow = new DefaultRowEdited(feat,
 							IRowEdited.STATUS_ADDED, i);
 
 					writer.process(edRow);
 				}
 			}
 
 		}
 
 		writer.postProcess();
 		progress.close();
 	}
 
 	public void saveToDxf(MapContext mapContext, FLyrVect layer)  {
 		try {
 			JFileChooser jfc = new JFileChooser(lastPath);
 			SimpleFileFilter filterShp = new SimpleFileFilter("dxf",
 					PluginServices.getText(this, "dxf_files"));
 			jfc.setFileFilter(filterShp);
 			if (jfc.showSaveDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
 				File newFile = jfc.getSelectedFile();
 				String path = newFile.getAbsolutePath();
 				if (!(path.toLowerCase().endsWith(".dxf"))) {
 					path = path + ".dxf";
 				}
 				newFile = new File(path);
 
 				DxfWriter writer = (DxfWriter) LayerFactory.getWM().getWriter(
 						"DXF Writer");
 				SHPLayerDefinition lyrDef = new SHPLayerDefinition();
 				SelectableDataSource sds = layer.getRecordset();
 				FieldDescription[] fieldsDescrip = sds.getFieldsDescription();
 				lyrDef.setFieldsDesc(fieldsDescrip);
 				lyrDef.setFile(newFile);
 				lyrDef.setName(newFile.getName());
 				lyrDef.setShapeType(layer.getShapeType());
 				writer.setFile(newFile);
 				writer.initialize(lyrDef);
 				writer.setProjection(layer.getProjection());
 				DxfFieldsMapping fieldsMapping = new DxfFieldsMapping();
 				// TODO: Recuperar aqu los campos del cuadro de dilogo.
 				writer.setFieldMapping(fieldsMapping);
 				DXFMemoryDriver dxfDriver=new DXFMemoryDriver();
 				dxfDriver.open(newFile);
 				writeFeatures(mapContext, layer, writer, dxfDriver);
 				String fileName = newFile.getAbsolutePath();
 				lastPath  = fileName.substring(0, fileName.lastIndexOf(File.separatorChar));
 			}
 
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		} catch (InitializeWriterException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		} catch (DriverLoadException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		}
 
 	}
 
 	public void saveToShp(MapContext mapContext, FLyrVect layer) {
 		try {
 			JFileChooser jfc = new JFileChooser();
 			SimpleFileFilter filterShp = new SimpleFileFilter("shp",
 					PluginServices.getText(this, "shp_files"));
 			jfc.setFileFilter(filterShp);
 			if (jfc.showSaveDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
 				File newFile = jfc.getSelectedFile();
 				String path = newFile.getAbsolutePath();
 				if( newFile.exists()){
 					int resp = JOptionPane.showConfirmDialog(
 							(Component) PluginServices.getMainFrame(),PluginServices.getText(this,"fichero_ya_existe_seguro_desea_guardarlo"),
 							PluginServices.getText(this,"guardar"), JOptionPane.YES_NO_OPTION);
 					if (resp != JOptionPane.YES_OPTION) {
 						return;
 					}
 				}
 				if (!(path.toLowerCase().endsWith(".shp"))) {
 					path = path + ".shp";
 				}
 				newFile = new File(path);
 
 
 
 				SelectableDataSource sds = layer.getRecordset();
 				FieldDescription[] fieldsDescrip = sds.getFieldsDescription();
 
 				if (layer.getShapeType() == FShape.MULTI) // Exportamos a 3
 				// ficheros
 				{
 					ShpWriter writer1 = (ShpWriter) LayerFactory.getWM().getWriter(
 					"Shape Writer");
 					Driver[] drivers=new Driver[3];
 					ShpWriter[] writers=new ShpWriter[3];
 
 					// puntos
 					String auxPoint = path.replaceFirst("\\.shp", "_points.shp");
 
 					SHPLayerDefinition lyrDefPoint = new SHPLayerDefinition();
 					lyrDefPoint.setFieldsDesc(fieldsDescrip);
 					File filePoints = new File(auxPoint);
 					lyrDefPoint.setFile(filePoints);
 					lyrDefPoint.setName(filePoints.getName());
 					lyrDefPoint.setShapeType(FShape.POINT);
 					writer1.setFile(filePoints);
 					writer1.initialize(lyrDefPoint);
 					writers[0]=writer1;
 					drivers[0]=getOpenShpDriver(filePoints);
 					//drivers[0]=null;
 
 
 
 					ShpWriter writer2 = (ShpWriter) LayerFactory.getWM().getWriter(
 					"Shape Writer");
 					// Lineas
 					String auxLine = path.replaceFirst("\\.shp", "_line.shp");
 					SHPLayerDefinition lyrDefLine = new SHPLayerDefinition();
 					lyrDefLine.setFieldsDesc(fieldsDescrip);
 
 					File fileLines = new File(auxLine);
 					lyrDefLine.setFile(fileLines);
 					lyrDefLine.setName(fileLines.getName());
 					lyrDefLine.setShapeType(FShape.LINE);
 					writer2.setFile(fileLines);
 					writer2.initialize(lyrDefLine);
 					writers[1]=writer2;
 					drivers[1]=getOpenShpDriver(fileLines);
 					//drivers[1]=null;
 
 					ShpWriter writer3 = (ShpWriter) LayerFactory.getWM().getWriter(
 					"Shape Writer");
 					// Polgonos
 					String auxPolygon = path.replaceFirst("\\.shp", "_polygons.shp");
 					SHPLayerDefinition lyrDefPolygon = new SHPLayerDefinition();
 					lyrDefPolygon.setFieldsDesc(fieldsDescrip);
 					File filePolygons = new File(auxPolygon);
 					lyrDefPolygon.setFile(filePolygons);
 					lyrDefPolygon.setName(filePolygons.getName());
 					lyrDefPolygon.setShapeType(FShape.POLYGON);
 					writer3.setFile(filePolygons);
 					writer3.initialize(lyrDefPolygon);
 					writers[2]=writer3;
 					drivers[2]=getOpenShpDriver(filePolygons);
 					//drivers[2]=null;
 
 					writeMultiFeatures(mapContext,layer, writers, drivers);
 				} else {
 					ShpWriter writer = (ShpWriter) LayerFactory.getWM().getWriter(
 						"Shape Writer");
 					IndexedShpDriver drv = getOpenShpDriver(newFile);
 					SHPLayerDefinition lyrDef = new SHPLayerDefinition();
 					lyrDef.setFieldsDesc(fieldsDescrip);
 					lyrDef.setFile(newFile);
 					lyrDef.setName(newFile.getName());
 					lyrDef.setShapeType(layer.getTypeIntVectorLayer());
 					writer.setFile(newFile);
 					writer.initialize(lyrDef);
 					// CODIGO PARA EXPORTAR UN SHP A UN CHARSET DETERMINADO
 					// ES UTIL PARA QUE UN DBF SE VEA CORRECTAMENTE EN EXCEL, POR EJEMPLO
 //					Charset resul = (Charset) JOptionPane.showInputDialog((Component)PluginServices.getMDIManager().getActiveWindow(),
 //								PluginServices.getText(ExportTo.class, "select_charset_for_writing"),
 //								"Charset", JOptionPane.QUESTION_MESSAGE, null,
 //								Charset.availableCharsets().values().toArray(),
 //								writer.getCharsetForWriting().displayName());
 //					if (resul == null)
 //						return;
 //					Charset charset = resul;
 //					writer.setCharsetForWriting(charset);
 					writeFeatures(mapContext, layer, writer, drv);
 
 				}
 			}
 		} catch (InitializeWriterException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		} catch (OpenDriverException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		} catch (DriverLoadException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		}
 
 	}
 
 	private IndexedShpDriver getOpenShpDriver(File fileShp) throws OpenDriverException {
 		IndexedShpDriver drv = new IndexedShpDriver();
 		if (!fileShp.exists()) {
 			try {
 				fileShp.createNewFile();
 				File newFileSHX=new File(fileShp.getAbsolutePath().replaceAll("[.]shp",".shx"));
 				newFileSHX.createNewFile();
 				File newFileDBF=new File(fileShp.getAbsolutePath().replaceAll("[.]shp",".dbf"));
 				newFileDBF.createNewFile();
 			} catch (IOException e) {
 				throw new FileNotFoundDriverException("SHP",e,fileShp.getAbsolutePath());
 			}
 		}
 		drv.open(fileShp);
 		return drv;
 	}
 	/**
 	 * @see com.iver.andami.plugins.IExtension#isEnabled()
 	 */
 	public boolean isEnabled() {
 		int status = EditionUtilities.getEditionStatus();
 		if (( status == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE || status == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE)
 				|| (status == EditionUtilities.EDITION_STATUS_MULTIPLE_VECTORIAL_LAYER_ACTIVE)|| (status == EditionUtilities.EDITION_STATUS_MULTIPLE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @see com.iver.andami.plugins.IExtension#isVisible()
 	 */
 	public boolean isVisible() {
 		com.iver.andami.ui.mdiManager.IWindow f = PluginServices.getMDIManager()
 				.getActiveWindow();
 
 		if (f == null) {
 			return false;
 		}
 
 		if (f instanceof View)
 			return true;
 		return false;
 	}
 
 	private int findFileByName(FieldDescription[] fields, String fieldName){
         for (int i=0; i < fields.length; i++)
         {
         	FieldDescription f = fields[i];
         	if (f.getFieldName().equalsIgnoreCase(fieldName))
         	{
         		return i;
         	}
         }
 
 		return -1;
 
 	}
 
 }
