 package cl.tival.check.view;
 
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cl.tival.check.model.Empresa;
 import cl.tival.check.model.Factura;
 import cl.tival.check.service.EmpresaService;
 import cl.tival.check.service.FacturaService;
 import cl.tival.check.service.PrinterService;
 import cl.tival.check.table.FacturaTableModel;
 
 public class MainPanel extends JPanel {
 	
 	public static Logger LOG = LoggerFactory.getLogger(MainPanel.class);
 	private static final long serialVersionUID = 1L;
 	private EmpresaService empresaService;
 	private FacturaService facturaService;
 	
 	private AgregarEmpresaDialog agregarEmpresaDialog;
 	private EliminarEmpresaDialog eliminarEmpresaDialog;
 	private ModificarEmpresaDialog modificarEmpresaDialog;
 	private AgregarFacturaDialog agregarFacturaDialog;
 	private EliminarFacturaDialog eliminarFacturaDialog;
 	private ModificarFacturaDialog modificarFacturaDialog;
 	private ImprimirDialog imprimirDialog;
	
     public void setImprimirDialog(ImprimirDialog imprimirDialog) {
 		this.imprimirDialog = imprimirDialog;
 	}
 
     public void setAgregarEmpresaDialog(AgregarEmpresaDialog agregarEmpresaDialog) {
 		this.agregarEmpresaDialog = agregarEmpresaDialog;
 	}
     
     public void setEliminarEmpresaDialog(EliminarEmpresaDialog eliminarEmpresaDialog) {
 		this.eliminarEmpresaDialog = eliminarEmpresaDialog;
 	}
 
     public void setModificarEmpresaDialog(ModificarEmpresaDialog modificarEmpresaDialog) {
 		this.modificarEmpresaDialog = modificarEmpresaDialog;
 	}
 
 	public void setAgregarFacturaDialog(AgregarFacturaDialog agregarFacturaDialog) {
 		this.agregarFacturaDialog = agregarFacturaDialog;
 	}
 
     public void setEliminarFacturaDialog(EliminarFacturaDialog eliminarFacturaDialog) {
 		this.eliminarFacturaDialog = eliminarFacturaDialog;
 	}
 
     public void setModificarFacturaDialog(ModificarFacturaDialog modificarFacturaDialog) {
 		this.modificarFacturaDialog = modificarFacturaDialog;
 	}
     
 	// Variables declaration - do not modify
     private javax.swing.JButton agregarEmpresaButton;
     private javax.swing.JButton agregarFacturaButton;
     private javax.swing.JButton eliminarEmpresaButton;
     private javax.swing.JButton eliminarFacturaButton;
     private javax.swing.JComboBox empresaComboBox;
     private javax.swing.JPanel empresaPanel;
     private javax.swing.JPanel facturasPanel;
     private javax.swing.JTable facturasTable;
     private javax.swing.JButton imprimirFacturasButton;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JButton modificarEmpresaButton;
     private javax.swing.JButton modificarFacturaButton;
     
     // End of variables declaration
 
 	public MainPanel(EmpresaService empresaService, FacturaService facturaService) {
 		this.empresaService = empresaService;
 		this.facturaService = facturaService;
         initComponents();
         updateMainPanel();
         //this.imprimirFacturasButton.setEnabled(Boolean.FALSE);
     }
 
     private void initComponents() {
 
         empresaPanel = new javax.swing.JPanel();
         empresaComboBox = new javax.swing.JComboBox();
         agregarEmpresaButton = new javax.swing.JButton();
         modificarEmpresaButton = new javax.swing.JButton();
         eliminarEmpresaButton = new javax.swing.JButton();
         facturasPanel = new javax.swing.JPanel();
         imprimirFacturasButton = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         facturasTable = new javax.swing.JTable();
         modificarFacturaButton = new javax.swing.JButton();
         agregarFacturaButton = new javax.swing.JButton();
         eliminarFacturaButton = new javax.swing.JButton();
 
         
 //        modificarEmpresaButton.setEnabled(false);
 //        modificarFacturaButton.setEnabled(false);
 //        eliminarEmpresaButton.setEnabled(false);
 //        eliminarFacturaButton.setEnabled(false);
         
         
         empresaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Empresa"));
 
         ///////////////////////empresaComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         empresaComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 empresaComboBoxActionPerformed(evt);
             }
         });
 
         agregarEmpresaButton.setText("Agregar");
         agregarEmpresaButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 agregarEmpresaButtonActionPerformed(evt);
             }
         });
 
         modificarEmpresaButton.setText("Modificar");
         modificarEmpresaButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 modificarEmpresaButtonActionPerformed(evt);
             }
         });
 
         eliminarEmpresaButton.setText("Eliminar");
         eliminarEmpresaButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eliminarEmpresaButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout empresaPanelLayout = new javax.swing.GroupLayout(empresaPanel);
         empresaPanel.setLayout(empresaPanelLayout);
         empresaPanelLayout.setHorizontalGroup(
             empresaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(empresaPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(empresaComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(agregarEmpresaButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(modificarEmpresaButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(eliminarEmpresaButton)
                 .addContainerGap())
         );
         empresaPanelLayout.setVerticalGroup(
             empresaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(empresaPanelLayout.createSequentialGroup()
                 .addGroup(empresaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(empresaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(agregarEmpresaButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(modificarEmpresaButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(eliminarEmpresaButton))
                 .addGap(0, 24, Short.MAX_VALUE))
         );
 
         facturasPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Facturas"));
 
         imprimirFacturasButton.setText("Imprimir");
         imprimirFacturasButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 imprimirFacturasButtonActionPerformed(evt);
             }
         });
 
         facturasTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane1.setViewportView(facturasTable);
 
         modificarFacturaButton.setText("Modificar");
         modificarFacturaButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 modificarFacturaButtonActionPerformed(evt);
             }
         });
 
         agregarFacturaButton.setText("Agregar");
         agregarFacturaButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 agregarFacturaButtonActionPerformed(evt);
             }
         });
 
         eliminarFacturaButton.setText("Eliminar");
         eliminarFacturaButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eliminarFacturaButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout facturasPanelLayout = new javax.swing.GroupLayout(facturasPanel);
         facturasPanel.setLayout(facturasPanelLayout);
         facturasPanelLayout.setHorizontalGroup(
             facturasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(facturasPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(facturasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
                     .addGroup(facturasPanelLayout.createSequentialGroup()
                         .addComponent(agregarFacturaButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(modificarFacturaButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(eliminarFacturaButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(imprimirFacturasButton)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         facturasPanelLayout.setVerticalGroup(
             facturasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(facturasPanelLayout.createSequentialGroup()
                 .addGroup(facturasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(imprimirFacturasButton)
                     .addComponent(modificarFacturaButton)
                     .addComponent(agregarFacturaButton)
                     .addComponent(eliminarFacturaButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(facturasPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(empresaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(empresaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(facturasPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
     }// </editor-fold>
 
     private void agregarEmpresaButtonActionPerformed(ActionEvent evt) {
     	System.out.println("ANTES de Agregar Empresa");
 		agregarEmpresaDialog.setVisible(true);
         this.updateMainPanel();
     	System.out.println("DESPUES de Agregar Empresa");
 	}
 
     private void modificarEmpresaButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	System.out.println("ANTES de Modificar Empresa");
     	Empresa empresa = (Empresa) empresaComboBox.getSelectedItem();
     	modificarEmpresaDialog.setEmpresa(empresa);
     	modificarEmpresaDialog.setVisible(true);
     	
         this.updateMainPanel();
     	System.out.println("DESPUES de Modificar Empresa");
     }
 
     private void eliminarEmpresaButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	System.out.println("ANTES de Eliminar Empresa");
     	Empresa empresa = (Empresa) empresaComboBox.getSelectedItem();
     	eliminarEmpresaDialog.setEmpresa(empresa);
     	eliminarEmpresaDialog.setVisible(true);
 		
         this.updateMainPanel();
     	System.out.println("DESPUES de Eliminar Empresa");
     }
 
     private void agregarFacturaButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	System.out.println("ANTES de Agregar Factura");
     	Empresa empresa = (Empresa) empresaComboBox.getSelectedItem();
     	agregarFacturaDialog.setEmpresa(empresa);
     	agregarFacturaDialog.setVisible(true);
         this.updateFacturas();
     	System.out.println("DESPUES de Agregar Factura");
     }
 
     private void modificarFacturaButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	LOG.trace("Starting \"modificarFacturaButtonActionPerformed\" method.");
     	int[] selectedRows = facturasTable.getSelectedRows();
     	if (selectedRows.length==1) {
     		int response = JOptionPane.showConfirmDialog(this, "¿Esta seguro de modificar la factura seleccionada?\n", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
     		if (response == JOptionPane.YES_OPTION) {
     	    	Empresa empresa = (Empresa) empresaComboBox.getSelectedItem();
     			Long numeroFactura = (Long) facturasTable.getValueAt(selectedRows[0], 0);
     			Factura factura = facturaService.getFactura(numeroFactura);
     			modificarFacturaDialog.setEmpresa(empresa);
     			modificarFacturaDialog.setFactura(factura);
     			modificarFacturaDialog.setVisible(true);
     		}
     	} else {
         	JOptionPane.showMessageDialog(this, "Para modificar debe seleccionar solo una factura", "Aviso", JOptionPane.WARNING_MESSAGE);
     	}
         this.updateMainPanel();
     	LOG.trace("Finishing \"modificarFacturaButtonActionPerformed\" method.");
     }
 
     private void eliminarFacturaButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	LOG.trace("Starting \"eliminarFacturaButtonActionPerformed\" method.");
     	int[] selectedRows = facturasTable.getSelectedRows();
     	if (selectedRows.length==1) {
     		int response = JOptionPane.showConfirmDialog(this, "¿Esta seguro de eliminar la factura seleccionada?\n", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
     		if (response == JOptionPane.YES_OPTION) {
     	    	Empresa empresa = (Empresa) empresaComboBox.getSelectedItem();
     			Long numeroFactura = (Long) facturasTable.getValueAt(selectedRows[0], 0);
     			Factura factura = facturaService.getFactura(numeroFactura);
     			eliminarFacturaDialog.setEmpresa(empresa);
     			eliminarFacturaDialog.setFactura(factura);
     			eliminarFacturaDialog.setVisible(true);
     		}
     	} else {
         	JOptionPane.showMessageDialog(this, "Para eliminar debe seleccionar solo una factura", "Aviso", JOptionPane.WARNING_MESSAGE);
     	}
         this.updateMainPanel();
     	LOG.trace("Finishing \"eliminarFacturaButtonActionPerformed\" method.");
     }
 
     private void imprimirFacturasButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	Object[] selectedRows = ((FacturaTableModel)facturasTable.getModel()).getSelectedIndexRow();
     	if(selectedRows.length>0) {
         	List<Long> numerosFactura = new ArrayList<Long>();
         	for(int i=0; i<selectedRows.length; i++) {
         		Long numeroFactura = (Long)facturasTable.getValueAt((Integer)selectedRows[i], 0);
         		numerosFactura.add(numeroFactura);
         	}
         	imprimirDialog.setNumerosFactura(numerosFactura);
         	imprimirDialog.cleanScreen();
         	imprimirDialog.setVisible(true);
        	//printerService.printFactura(numerosFactura);

     		int response = JOptionPane.showConfirmDialog(this, "¿La impresión fue exitosa?\nSi selecciona \"No\" las facturas no serán eliminadas de la tabla.", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
             if (response == JOptionPane.YES_OPTION ) {
            	//printerService.marcarFacturaimpresa(numerosFactura);
             	updateFacturas();
             }
         } else {
         	JOptionPane.showMessageDialog(this, "Para imprimir debe seleccionar al menos una factura", "Aviso", JOptionPane.WARNING_MESSAGE);
         }
     }
 
     private void empresaComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
     	updateFacturas();
     }
     
     private void updateMainPanel() {
     	Empresa empresa = (Empresa) empresaComboBox.getSelectedItem();
     	this.empresaComboBox.setModel(this.empresaService.getComboBoxEmpresasActivas());
     	System.out.println("EMPRESA: "+empresa);
     	if(empresa!=null) {
         	System.out.println("No es null");
     		this.empresaComboBox.setSelectedItem(empresa);
     	}    	
     	updateFacturas();
     }
     
     private void updateFacturas() {
     	Empresa empresa = (Empresa) empresaComboBox.getSelectedItem();
     	if(empresa != null) {
 	    	System.out.println(empresa.getRut());
 	    	this.facturasTable.setModel(facturaService.getFacturasActivasTableModel(empresa.getRut()));
     	} else {
     		this.facturasTable.setModel(new FacturaTableModel());
     	}
     }
     
 }
