 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * FormPengembalian.java
  *
  * Created on Oct 7, 2012, 9:53:09 AM
  */
 package perpus.ui.transaksi;
 
 import java.awt.Component;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.swing.JOptionPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableCellRenderer;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeComparator;
 import org.springframework.util.StringUtils;
 import perpus.Main;
 import perpus.domain.Buku;
 import perpus.domain.Konfigurasi;
 import perpus.domain.Peminjaman;
 import perpus.domain.PeminjamanDetail;
 import perpus.domain.Pengembalian;
 import perpus.domain.PengembalianDetail;
 import perpus.ui.TableUtil;
 import perpus.ui.tablemodel.PengembalianDetailTableModel;
 import perpus.util.BigDecimalTableRenderer;
 import perpus.util.ErrorDialog;
 import perpus.util.TextComponentUtils;
 
 /**
  *
  * @author adi
  */
 public class FormPengembalian extends javax.swing.JPanel {
 
     public static final String PANEL_NAME = "Transaksi Pengembalian";
     private static FormPengembalian panel;
     private Peminjaman peminjaman;
     private Pengembalian pengembalian;
     private PengembalianDetail detail;
     private List<PengembalianDetail> detailPengembalians = new ArrayList<PengembalianDetail>();
     private List<PengembalianDetail> detailPengembaliansOrig = new ArrayList<PengembalianDetail>();
     private Date sekarang = new Date();
 
     /**
      * Creates new form FormPengembalian
      */
     public FormPengembalian() {
         initComponents();
         TextComponentUtils.setCurrency(txtTotalDenda);
         tbl.setDefaultRenderer(BigDecimal.class, new BigDecimalTableRenderer());
         tbl.getSelectionModel().addListSelectionListener(new TableSelection());
     }
 
     public static FormPengembalian getPanel() {
         if (panel == null) {
             panel = new FormPengembalian();
         }
         return panel;
     }
 
     void clearForm() {
         peminjaman = null;
         pengembalian = null;
         detailPengembalians = new ArrayList<PengembalianDetail>();
         detailPengembaliansOrig = new ArrayList<PengembalianDetail>();
         txtTransaksi.setText("");
         txtTotalDenda.setText("");
         tglPinjam.setDate(null);
 
         refreshTableDetail();
     }
 
     public static Integer hitungHari(Date from, Date to) {
         DateTime sampai = new DateTime(to);
 
         DateTimeComparator comparator = DateTimeComparator.getDateOnlyInstance();
         DateTime current = new DateTime(from);
 
         Integer jumlahHari = 0;
         if (comparator.compare(current, sampai) < 0) {
             while (comparator.compare(current, sampai) != 0) {
                 jumlahHari++;
                 current = current.plusDays(1);
             }
         }
 
         return jumlahHari;
     }
 
     void loadToDetailPengembalian() {
         List<PengembalianDetail> listSudahDikembalikan =
                 Main.getTransaksiService().getTransaksiPengembalianByIdPinjam(peminjaman.getId());
 
         for (PeminjamanDetail d1 : peminjaman.getDetailPeminjamans()) {
            Konfigurasi config = Main.getMasterService().findKonfigurasiByKode(d1.getBuku().getJenisBuku().getKode());
             Integer telat = hitungHari(d1.getTglKembali(), sekarang);
             boolean allowAdd = true;
             for (PengembalianDetail d2 : listSudahDikembalikan) {
                 if (d1.getBuku().getKodeBuku().equalsIgnoreCase(d2.getBuku().getKodeBuku())) {
                     allowAdd = false;
                     break;
                 }
             }
             if (allowAdd) {
                 PengembalianDetail detail = new PengembalianDetail();
                 detail.setBuku(d1.getBuku());
                 detail.setTelat(telat);
                 detail.setTglKembaliRealisasi(sekarang);
                 BigDecimal denda = config.getDendaPerHari()
                         .multiply(new BigDecimal(detail.getTelat()));
                 detail.setDenda(denda);
                 detailPengembalians.add(detail);
                 detailPengembaliansOrig.add(detail);
             }
         }
         refreshTableDetail();
     }
 
     void refreshTableDetail() {
         tbl.setModel(new PengembalianDetailTableModel(detailPengembalians, peminjaman));
         TableUtil.initColumn(tbl);
         hitungTotalDenda();
     }
 
     void hitungTotalDenda() {
         BigDecimal total = BigDecimal.ZERO;
         for (PengembalianDetail d : detailPengembalians) {
             total = total.add(d.getDenda());
         }
 
         txtTotalDenda.setText(TextComponentUtils.formatNumber(total));
     }
 
     Boolean cekItemExisted(Buku buku) {
         Boolean retval = true;
         for (PengembalianDetail d : detailPengembalians) {
             if (d.getBuku().getKodeBuku().equalsIgnoreCase(buku.getKodeBuku())) {
                 JOptionPane.showMessageDialog(Main.getMainForm(),
                         "Buku sudah dipilih !");
                 retval = false;
                 break;
             }
         }
         return retval;
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         btnAdd = new javax.swing.JButton();
         btnDelete = new javax.swing.JButton();
         jScrollPane2 = new javax.swing.JScrollPane();
         tbl = new javax.swing.JTable() {
             public Component prepareRenderer(
                 TableCellRenderer renderer, int row, int column) {
                 Component c = super.prepareRenderer(renderer, row, column);
 
                 if (!isRowSelected(row)) {
                     c.setBackground(row % 2 == 0 ? getBackground() : new java.awt.Color(237, 243, 254));
                 }
 
                 return c;
             }
         };
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         jLabel5 = new javax.swing.JLabel();
         txtTransaksi = new javax.swing.JTextField();
         tglPinjam = new com.toedter.calendar.JDateChooser();
         txtTotalDenda = new javax.swing.JTextField();
         btnLookupTransaksi = new javax.swing.JButton();
         btnBatal = new javax.swing.JButton();
         btnSave = new javax.swing.JButton();
         btnClose = new javax.swing.JButton();
 
         btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/perpus/img/add.gif"))); // NOI18N
         btnAdd.setToolTipText("Tambah Buku");
         btnAdd.setFocusable(false);
         btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddActionPerformed(evt);
             }
         });
 
         btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/perpus/img/delete.gif"))); // NOI18N
         btnDelete.setToolTipText("Hapus Buku");
         btnDelete.setFocusable(false);
         btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnDelete.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDeleteActionPerformed(evt);
             }
         });
 
         tbl.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Kode", "Judul", "Telat", "Denda"
             }
         ));
         tbl.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         tbl.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane2.setViewportView(tbl);
 
         jLabel1.setText("Transaksi Peminjaman");
 
         jLabel2.setText("Tgl Pinjam");
 
         jLabel5.setText("Total Denda");
 
         txtTransaksi.setEnabled(false);
 
         tglPinjam.setEnabled(false);
 
         txtTotalDenda.setEnabled(false);
 
         btnLookupTransaksi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/perpus/img/lookup_icon.gif"))); // NOI18N
         btnLookupTransaksi.setToolTipText("Lookup Transaksi Peminjaman");
         btnLookupTransaksi.setFocusable(false);
         btnLookupTransaksi.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnLookupTransaksi.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnLookupTransaksi.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnLookupTransaksiActionPerformed(evt);
             }
         });
 
         btnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/perpus/img/reload.png"))); // NOI18N
         btnBatal.setToolTipText("Batal Transaksi");
         btnBatal.setFocusable(false);
         btnBatal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnBatal.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnBatal.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBatalActionPerformed(evt);
             }
         });
 
         btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/perpus/img/save.png"))); // NOI18N
         btnSave.setToolTipText("Simpan Transaksi");
         btnSave.setFocusable(false);
         btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveActionPerformed(evt);
             }
         });
 
         btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/perpus/img/close.gif"))); // NOI18N
         btnClose.setToolTipText("Tutup Form");
         btnClose.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCloseActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                         .addContainerGap())
                     .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel1)
                             .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel2)
                                     .addComponent(jLabel5))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(txtTotalDenda, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(btnClose))
                                     .addGroup(layout.createSequentialGroup()
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                             .addComponent(txtTransaksi, javax.swing.GroupLayout.Alignment.LEADING)
                                             .addComponent(tglPinjam, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(btnLookupTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                         .addContainerGap(136, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(389, 389, 389))))
         );
 
         layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel5});
 
         layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {tglPinjam, txtTotalDenda});
 
         layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAdd, btnBatal, btnClose, btnDelete, btnLookupTransaksi, btnSave});
 
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel1)
                             .addComponent(txtTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel2)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(tglPinjam, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jLabel5)
                                     .addComponent(txtTotalDenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                     .addComponent(btnLookupTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnClose))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnAdd, btnBatal, btnClose, btnDelete, btnLookupTransaksi, btnSave, jLabel1, jLabel2, jLabel5, tglPinjam, txtTotalDenda, txtTransaksi});
 
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
         if(detailPengembalians.size() == detailPengembaliansOrig.size()){
             JOptionPane.showMessageDialog(Main.getMainForm(),
                         "Semua data sudah terpilih !");
             return;
         }
         System.out.println("detailPengembaliansOrig : " + detailPengembaliansOrig.size());
         PengembalianDetail detail = new TambahDetailPengembalianDialog(detailPengembaliansOrig, peminjaman).showDialog();
         if (detail != null) {
             if (cekItemExisted(detail.getBuku())) {
                 detailPengembalians.add(detail);
             }
         }
         refreshTableDetail();
 }//GEN-LAST:event_btnAddActionPerformed
 
     private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
         detailPengembalians.remove(detail);
         refreshTableDetail();
         hitungTotalDenda();
 }//GEN-LAST:event_btnDeleteActionPerformed
 
     private void btnLookupTransaksiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLookupTransaksiActionPerformed
         peminjaman = new LookupTransaksiPeminjamanDialog().showDialog();
         if (peminjaman != null) {
             StringBuilder text = new StringBuilder();
             text.append(peminjaman.getId());
             text.append(" | ");
             text.append(peminjaman.getAnggota().getNamaAnggota());
             text.append(" | ");
             text.append(peminjaman.getDetailPeminjamans().size());
             text.append(" items");
             txtTransaksi.setText(text.toString());
 
             tglPinjam.setDate(peminjaman.getTglPinjam());
             loadToDetailPengembalian();
         }
 }//GEN-LAST:event_btnLookupTransaksiActionPerformed
 
     private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalActionPerformed
         clearForm();
 }//GEN-LAST:event_btnBatalActionPerformed
 
     private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
         try {
             if (validateForm()) {
                 loadFormToDomain();
                 Main.getTransaksiService().save(pengembalian);
                 clearForm();
                 JOptionPane.showMessageDialog(Main.getMainForm(),
                         "Transaksi berhasil disimpan !");
             } else {
                 JOptionPane.showMessageDialog(Main.getMainForm(),
                         "Data yang anda masukan tidak lengkap !");
             }
         } catch (Exception e) {
             ErrorDialog.showErrorDialog(e);
         }
 }//GEN-LAST:event_btnSaveActionPerformed
 
     private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
         Main.getMainForm().getMainTabbedPane().remove(this);
         panel = null;
     }//GEN-LAST:event_btnCloseActionPerformed
 
     void loadFormToDomain() {
         pengembalian = new Pengembalian();
         pengembalian.setTransaksiPeminjaman(peminjaman);
         pengembalian.setTotalDenda(TextComponentUtils.parseNumberToBigDecimal(txtTotalDenda.getText()));
         pengembalian.setPegawai(Main.getPegawai());
         for (PengembalianDetail d : detailPengembalians) {
             d.setHeader(pengembalian);
         }
         pengembalian.setDetailsPengembalian(detailPengembalians);
     }
 
     Boolean validateForm() {
         if (peminjaman != null
                 && tglPinjam != null
                 && StringUtils.hasText(txtTotalDenda.getText())
                 && detailPengembalians.size() > 0) {
             return true;
         }
         return false;
     }
 
     private class TableSelection implements ListSelectionListener {
 
         @Override
         public void valueChanged(ListSelectionEvent e) {
             if (e.getValueIsAdjusting()) {
                 return;
             }
 
             if (tbl.getSelectedRow() >= 0) {
                 detail = detailPengembalians.get(tbl.getSelectedRow());
             }
 
         }
     }
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAdd;
     private javax.swing.JButton btnBatal;
     private javax.swing.JButton btnClose;
     private javax.swing.JButton btnDelete;
     private javax.swing.JButton btnLookupTransaksi;
     private javax.swing.JButton btnSave;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JTable tbl;
     private com.toedter.calendar.JDateChooser tglPinjam;
     private javax.swing.JTextField txtTotalDenda;
     private javax.swing.JTextField txtTransaksi;
     // End of variables declaration//GEN-END:variables
 }
