 package jp.seraphyware.cryptnotepad.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.FlowLayout;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.dnd.DropTarget;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyVetoException;
 import java.io.File;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDesktopPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JSplitPane;
 import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
 
 import jp.seraphyware.cryptnotepad.crypt.CryptUtils;
 import jp.seraphyware.cryptnotepad.model.ApplicationData;
 import jp.seraphyware.cryptnotepad.model.ApplicationSettings;
 import jp.seraphyware.cryptnotepad.model.DocumentController;
 import jp.seraphyware.cryptnotepad.model.SettingsModel;
 import jp.seraphyware.cryptnotepad.model.DocumentController.PassphraseUIProvider;
 import jp.seraphyware.cryptnotepad.util.ErrorMessageHelper;
 import jp.seraphyware.cryptnotepad.util.FileDropTarget;
 import jp.seraphyware.cryptnotepad.util.XMLResourceBundle;
 
 /**
  * メインフレーム
  * 
  * @author seraphy
  */
 public class MainFrame extends JFrame implements PassphraseUIProvider {
 
     private static final long serialVersionUID = 8358190990080417295L;
 
     /**
      * ロガー.<br>
      */
     private static final Logger logger = Logger.getLogger(MainFrame.class
             .getName());
 
     /**
      * アプリケーション設定
      */
     private ApplicationSettings appConfig;
 
     /**
      * リソースバンドル
      */
     private ResourceBundle resource;
 
     /**
      * ドキュメントコントローラ
      */
     private DocumentController documentController;
 
     /**
      * MDIフレーム(デスクトップ)
      */
     private JDesktopPane desktop;
 
     /**
      * ファイルツリーパネル
      */
     private FileTreePanel fileTreePanel;
 
     /**
      * コンストラクタ
      */
     public MainFrame(DocumentController documentController) {
         try {
             if (documentController == null) {
                 throw new IllegalArgumentException();
             }
 
             this.documentController = documentController;
             this.appConfig = ApplicationSettings.getInstance();
             resource = ResourceBundle.getBundle(getClass().getName(),
                     XMLResourceBundle.CONTROL);
 
             init();
 
         } catch (RuntimeException ex) {
             dispose();
             throw ex;
         }
     }
 
     /**
      * フレームを初期化する.
      */
     private void init() {
         // ウィンドウの閉じるイベント
         setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 onClosing();
             }
         });
 
         // タイトル
         setTitle(resource.getString("mainframe.title"));
 
         // MDIフレーム
         desktop = new JDesktopPane();
         desktop.setBackground(Color.lightGray);
 
         // ファイル一覧パネル
         fileTreePanel = new FileTreePanel();
         fileTreePanel.refresh();
         JPanel leftPanel = createFileTreePanel(fileTreePanel);
 
         // パスフレーズの入力・確認が必要な場合なハンドラを設定する.
         documentController.setPassphraseUiProvider(this);
 
         // 文書ディレクトリが変更された場合はリフレッシュする.
         appConfig.addPropertyChangeListener("contentsDir",
                 new PropertyChangeListener() {
                     @Override
                     public void propertyChange(PropertyChangeEvent evt) {
                         // ファイル一覧を更新する.
                         fileTreePanel.refresh();
 
                         // ファイル格納先のディレクトリもあわせておく.
                         File dir = (File) evt.getNewValue();
                         DocumentInternalFrame.setLastUseEncryptedDir(dir);
                     }
                 });
 
         // レイアウト
         Container contentPane = getContentPane();
         contentPane.setLayout(new BorderLayout());
 
         JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
         splitPane.setDividerLocation(200);
 
         splitPane.add(leftPanel);
         splitPane.add(desktop);
 
         contentPane.add(splitPane);
 
         // ファイルのドロップを許可する.
         // ドロップターゲットの設定
         new DropTarget(this, new FileDropTarget() {
             @Override
             protected void onDropFiles(final List<File> dropFiles) {
                 if (dropFiles == null || dropFiles.isEmpty()) {
                     return;
                 }
                 // インポートダイアログを開く.
                 // ドロップソースの処理がブロッキングしないように、
                 // ドロップハンドラの処理を終了してからインポートダイアログが開くようにする.
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         for (File dropFile : dropFiles) {
                             if (dropFile.isFile()) {
                                 openPlainFile(dropFile);
                             }
                         }
                     }
                 });
             }
 
             @Override
             protected void onException(Exception ex) {
                 ErrorMessageHelper.showErrorDialog(MainFrame.this, ex);
             }
         });
     }
 
     /**
      * 設定されているパスフレーズを照合する.
      */
     @Override
     public boolean verifyPassphrase(SettingsModel settingsModel) {
         String title = resource.getString("verifyPassphrase.title");
         for (;;) {
            final JPasswordField txtPassphrase = new JPasswordField();
            txtPassphrase.addAncestorListener(new AncestorListener() {
                @Override
                public void ancestorRemoved(AncestorEvent event) {
                    // do nothing.
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                    // do nothing.
                }

                @Override
                public void ancestorAdded(AncestorEvent event) {
                    // JOptionPaneに組み込まれたときにフォーカスをリクエストする.
                    txtPassphrase.requestFocusInWindow();
                }
            });
             int ret = JOptionPane.showConfirmDialog(this, txtPassphrase, title,
                     JOptionPane.OK_CANCEL_OPTION);
             if (ret != JOptionPane.OK_OPTION) {
                 // キャンセル
                 return false;
             }
             char[] verifyPassphrase = txtPassphrase.getPassword();
             char[] passphrase = settingsModel.getPassphrase();
             if (verifyPassphrase != null && passphrase != null
                     && Arrays.equals(verifyPassphrase, passphrase)) {
                 return true;
             }
         }
     }
 
     /**
      * パスフレーズの有無を確認し、設定されていなければ設定ダイアログを開けるようにする.<br>
      * パスフレーズを設定されない場合はfalseを返す.<br>
      * 
      * @return パスフレーズが設定されていればtrue、されなかったらfalse
      */
     @Override
     public boolean requirePassphrase(SettingsModel settingsModel) {
         while (!settingsModel.isValid()) {
             // パスフレーズが未設定であればエラー表示し、設定画面を開くか問い合わせる.
             String message = resource.getString("error.password.required");
             String title = resource.getString("confirm.title");
             int ret = JOptionPane.showConfirmDialog(this, message, title,
                     JOptionPane.YES_NO_OPTION);
             if (ret != JOptionPane.YES_OPTION) {
                 // 設定画面を開かない場合はfalseを返す.
                 return false;
             }
             // 設定画面を開く.(モーダル)
             onSettings();
         }
         return true;
     }
 
     /**
      * 暗号化されたファイルをロードする.<br>
      * 引数がnullの場合はnullを返す.<br>
      * 
      * @param file
      *            暗号されたファイル
      * @return 復号化されたデータ、もしくはnull
      */
     protected Object loadEncrypted(File file) {
         if (file != null) {
             try {
                 // ファイルをロードする.
                 // 外部URLのファイルハッシュつきの場合は復号化に時間がかかるのでウェイトカーソルをつける.
                 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 try {
                     return documentController.decrypt(file);
 
                 } finally {
                     setCursor(Cursor.getDefaultCursor());
                 }
 
             } catch (Exception ex) {
                 // ロードに失敗したらエラー表示し、ウィンドウは開かない.
                 ErrorMessageHelper.showErrorDialog(this, ex);
                 return null;
             }
         }
         return null;
     }
 
     /**
      * テキストドキュメント用のMDI子ウィンドウを開く
      * 
      * @param file
      *            対象ファイル、nullの場合は新規ドキュメントを開く.
      * @param doc
      *            テキスト
      * 
      * @return 生成された子ウィンドウ、生成できなければnull
      */
     protected TextInternalFrame createTextInternalFrame(File file, String doc) {
         // テキスト編集用の子ウィンドウを作成する.
         final TextInternalFrame internalFrame = new TextInternalFrame(
                 documentController);
 
         // テキストとファイル名を設定する.
         internalFrame.setText(doc);
         internalFrame.setFile(file);
 
         addInternalFrame(internalFrame);
 
         return internalFrame;
     }
 
     /**
      * 画像表示用のMDI子ウィンドウを開く.
      * 
      * @param file
      *            対象ファイル、nullの場合は新規ドキュメントを開く.
      * @param data
      *            バイナリデータ(画像)
      * @return 生成された子ウィンドウ、生成できなければnull
      */
     protected PictureInternalFrame createPictureInternalFrame(File file,
             ApplicationData data) {
         // 画像表示用の子ウィンドウを作成する.
         final PictureInternalFrame internalFrame = new PictureInternalFrame(
                 documentController);
 
         // テキストとファイル名を設定する.
         internalFrame.setData(data);
         internalFrame.setFile(file);
 
         addInternalFrame(internalFrame);
 
         return internalFrame;
     }
 
     /**
      * MDI子ウィンドウを登録する共通処理.<br>
      * 登録に際して、ファイル名変更リスナを設定したり、画面サイズを設定する.<br>
      * 
      * @param internalFrame
      */
     protected void addInternalFrame(final DocumentInternalFrame internalFrame) {
         if (internalFrame == null) {
             throw new IllegalArgumentException();
         }
         // ファイル名が変更されたら通知を受け取るようにリスナを設定する.
         internalFrame.addPropertyChangeListener(
                 TextInternalFrame.PROPERTY_FILE, new PropertyChangeListener() {
                     @Override
                     public void propertyChange(PropertyChangeEvent evt) {
                         // ドキュメントのファイル名が変更された場合
                         onChangeFileName(internalFrame,
                                 (File) evt.getOldValue(),
                                 (File) evt.getNewValue());
                     }
                 });
 
         desktop.add(internalFrame);
 
         internalFrame.setSize(200, 200);
         internalFrame.setLocation(0, 0);
         internalFrame.setVisible(true);
 
         try {
             internalFrame.setMaximum(true);
 
         } catch (PropertyVetoException ex) {
             logger.log(Level.FINE, ex.toString());
         }
     }
 
     /**
      * ファイル一覧パネルを作成する.<br>
      * 
      * @return
      */
     private JPanel createFileTreePanel(final FileTreePanel fileTreePanel) {
 
         fileTreePanel.setBorder(BorderFactory.createTitledBorder(resource
                 .getString("files.border.title")));
 
         fileTreePanel.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 // ダブルクリックされた場合、「選択ファイル」をオープンする.
                 onOpenFile(fileTreePanel.getSelectedFile());
             }
         });
 
         final JPanel leftPanel = new JPanel(new BorderLayout());
 
         JButton btnSettings = new JButton(new AbstractAction(
                 resource.getString("settings.button.title")) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 // 設定ダイアログを開く.
                 onSettings();
             }
         });
 
         JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         JButton btnNew = new JButton(new AbstractAction(
                 resource.getString("new.button.title")) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 // 新規ドキュメントを開く
                 onNew();
             }
         });
         btnNew.setToolTipText(resource.getString("new.button.tooltip"));
 
         JButton btnDelete = new JButton(new AbstractAction(
                 resource.getString("delete.button.title")) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                     // シフトキーとともにある場合は任意ファイルを削除する.
                     onDeleteAny();
 
                 } else {
                     // 通常の場合はフォーカスされたアイテムを削除する.
                     onDelete(fileTreePanel.getFocusedFile());
                 }
             }
         });
         btnDelete.setToolTipText(resource.getString("delete.button.tooltip"));
 
         JButton btnOpen = new JButton(new AbstractAction(
                 resource.getString("open.button.title")) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 // 「開く」ボタンを押された場合
                 if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                     // シフトキーとともにある場合は任意ファイルを開く.
                     onOpenAny();
 
                 } else {
                     // 通常の場合はフォーカスされたアイテムをオープンする.
                     onOpenFile(fileTreePanel.getFocusedFile());
                 }
             }
         });
         btnOpen.setToolTipText(resource.getString("open.button.tooltip"));
 
         btnPanel.add(btnNew);
         btnPanel.add(btnDelete);
         btnPanel.add(btnOpen);
 
         leftPanel.add(btnSettings, BorderLayout.NORTH);
         leftPanel.add(fileTreePanel, BorderLayout.CENTER);
         leftPanel.add(btnPanel, BorderLayout.SOUTH);
 
         return leftPanel;
     }
 
     /**
      * 設定ボタンハンドラ.<br>
      * パスフレーズやキーファイル、文字コードなどの設定を行う.<br>
      */
     protected void onSettings() {
         SettingsDialog settingsDlg = new SettingsDialog(this);
         settingsDlg.setLocationRelativeTo(this);
         settingsDlg.setModel(documentController.getSettingsModel());
         settingsDlg.setVisible(true);
     }
 
     /**
      * ドキュメント名が変更されたことを通知される.<br>
      * 
      * @param internalFrame
      *            ドキュメントのウィンドウ
      * @param oldFile
      *            以前のファイル名、なければnull
      * @param newFile
      *            新しいファイル名
      */
     protected void onChangeFileName(JInternalFrame internalFrame, File oldFile,
             File newFile) {
         // ドキュメントツリーをリフレッシュする.
         fileTreePanel.refresh();
     }
 
     /**
      * 新規用に暗号化用のテキストウィンドウを開く.
      */
     protected void onNew() {
         // 新規にテキストドキュメントを開く.
         createTextInternalFrame(null, null);
     }
 
     /**
      * 暗号化されたテキストドキュメント用のウィンドウを開く.
      * 
      * @param file
      *            ファイル 、nullの場合は何もしない.
      */
     protected void onOpenFile(File file) {
         if (file == null) {
             return;
         }
 
         for (JInternalFrame child : desktop.getAllFrames()) {
             if (!child.isDisplayable() || !child.isVisible()) {
                 // 表示されていないか、破棄されたものは除外する.
                 continue;
             }
             if (child instanceof DocumentInternalFrame) {
                 DocumentInternalFrame c = (DocumentInternalFrame) child;
                 File docFile = c.getFile();
                 if (docFile != null && docFile.equals(file)) {
                     // すでに同ファイルがオープンされていれば、それをアクティブにする.
                     // (別名保存などで複数同名ファイルが開かれた状態の場合は最初の一つ)
                     desktop.getDesktopManager().activateFrame(c);
                     return;
                 }
             }
         }
 
         // まだファイルが開かれていなければ開く.
         Object data = loadEncrypted(file);
         if (data != null && data instanceof String) {
             // テキストデータの場合
             createTextInternalFrame(file, (String) data);
 
         } else if (data != null && data instanceof ApplicationData) {
             // バイナリデータの場合(画像)
             createPictureInternalFrame(file, (ApplicationData) data);
         }
     }
 
     /**
      * 任意の非暗号化ファイルを開く.
      */
     protected void onOpenAny() {
         JFileChooser fileChooser = FileChooserEx.createFileChooser(
                 appConfig.getLastUseDir(), false);
         fileChooser.setMultiSelectionEnabled(true);
 
         int ret = fileChooser.showOpenDialog(this);
         if (ret != JFileChooser.APPROVE_OPTION) {
             return;
         }
 
         File[] files = fileChooser.getSelectedFiles();
         if (files == null || files.length == 0) {
             // 選択なし
             return;
         }
 
         // 最後に使用したディレクトリとして記憶する.(代表として先頭ファイル)
         appConfig.setLastUseDir(files[0].getParentFile());
 
         // 複数ファイルを連続して開く.
         for (File file : files) {
             openPlainFile(file);
         }
     }
 
     /**
      * 非暗号化ファイルを開く.
      * 
      * @param file
      *            ファイル
      */
     protected void openPlainFile(File file) {
         if (file == null || !file.exists()) {
             return;
         }
 
         // ファイル名からMIMEタイプを判定する.
         String contentType = documentController.detectContentType(file);
         if (contentType == null) {
             // 不明であれば手動で選択する.
             contentType = chooseContentType(file);
             if (contentType == null) {
                 // キャンセルされた場合
                 return;
             }
         }
 
         if (!contentType.startsWith("text/")) {
             // テキスト以外(画像とバイナリ)の場合はバイナリとして読み込む
             byte[] buf;
             try {
                 buf = documentController.loadBinary(file);
 
             } catch (Exception ex) {
                 ErrorMessageHelper.showErrorDialog(this, ex);
                 return;
             }
 
             if (buf == null) {
                 return;
             }
 
             // バイナリデータの構築
             ApplicationData data = new ApplicationData(contentType, buf);
 
             // 新規にバイナリドキュメントを開く.
             PictureInternalFrame internalFrame = createPictureInternalFrame(
                     null, data);
 
             // 読み込んだファイルの内容とファイル名を設定する.
             // (未保存のドキュメントとして扱われる)
             internalFrame.setTemporaryTitle(file.getName());
             internalFrame.setModified(true); // 編集中としてマークする.
 
         } else {
             // テキストの場合は、読み込み文字コードを選択する.
             String encoding = chooseCharset();
             if (encoding == null) {
                 return;
             }
 
             // 平文ファイルを読み込む
             String doc;
             try {
                 doc = documentController.loadText(file, encoding);
 
             } catch (Exception ex) {
                 ErrorMessageHelper.showErrorDialog(this, ex);
                 return;
             }
 
             // 新規にテキストドキュメントを開く.
             TextInternalFrame internalFrame = createTextInternalFrame(null, doc);
 
             // 読み込んだファイルの内容とファイル名を設定する.
             // (未保存のドキュメントとして扱われる)
             internalFrame.setTemporaryTitle(file.getName());
             internalFrame.setModified(true); // 編集中としてマークする.
         }
     }
 
     /**
      * 手動でContent-Typeを選択する.
      * 
      * @param file
      *            ファイル
      * @return 選択されたContentType、キャンセルした場合はnull
      */
     protected String chooseContentType(File file) {
         if (file == null) {
             throw new IllegalArgumentException();
         }
 
         String lcFileName = file.getName().toLowerCase();
         int pt = lcFileName.lastIndexOf('.');
         String ext = lcFileName.substring(pt + 1);
 
         String[] options = { "text/plain", "text/" + ext, "image/" + ext,
                 "application/octet-stream", "application/" + ext };
 
         JComboBox optionsCombo = new JComboBox(options);
         String title = resource.getString("selectContentType.dialog.title");
         int ret = JOptionPane.showConfirmDialog(this, optionsCombo, title,
                 JOptionPane.YES_NO_OPTION);
         if (ret != JOptionPane.YES_OPTION) {
             return null;
         }
         return (String) optionsCombo.getSelectedItem();
     }
 
     /**
      * 外部ファイル取り込み用の文字コードを選択する.
      * 
      * @return 文字コード、キャンセルした場合はnull
      */
     protected String chooseCharset() {
         // テキストタイプ
         // サポートされている文字コード一覧の取得
         ArrayList<String> encodingNames = new ArrayList<String>();
         for (String name : Charset.availableCharsets().keySet()) {
             encodingNames.add(name);
         }
         String defaultEncoding = appConfig.getLastUseImportTextEncoding();
         if (defaultEncoding == null || defaultEncoding.trim().length() == 0) {
             defaultEncoding = Charset.defaultCharset().name();
         }
 
         // 文字コードの選択ダイアログ
         JComboBox encodingCombo = new JComboBox(
                 encodingNames.toArray(new String[encodingNames.size()]));
         encodingCombo.setSelectedItem(defaultEncoding);
 
         int ret = JOptionPane.showConfirmDialog(this, encodingCombo,
                 "Choose Charset", JOptionPane.YES_NO_OPTION);
 
         String encoding = null;
         if (ret == JOptionPane.YES_OPTION) {
             encoding = (String) encodingCombo.getSelectedItem();
             if (encoding != null) {
                 appConfig.setLastUseImportTextEncoding(encoding);
             }
         }
         return encoding;
     }
 
     /**
      * 任意のファイルを選択して削除する.
      */
     protected void onDeleteAny() {
         JFileChooser fileChooser = FileChooserEx.createFileChooser(
                 appConfig.getLastUseDir(), false);
 
         // 複数選択可
         fileChooser.setMultiSelectionEnabled(true);
         fileChooser.setDialogTitle(resource.getString("secureerase.title"));
 
         int ret = fileChooser.showOpenDialog(this);
         if (ret != JFileChooser.APPROVE_OPTION) {
             return;
         }
 
         File[] files = fileChooser.getSelectedFiles();
         if (files.length == 0) {
             // 選択なし
             return;
         }
 
         // 確認ダイアログ
         String title = resource.getString("confirm.title");
         String messageTmpl = resource.getString("confirm.erase.file");
         String message = String.format(messageTmpl,
                 String.format("%d files", files.length));
 
         ret = JOptionPane.showConfirmDialog(this, message, title,
                 JOptionPane.YES_NO_OPTION);
         if (ret == JOptionPane.YES_OPTION) {
             // 選択されたファイルをすべて削除する.
             for (File file : files) {
                 try {
                     if (file.exists() && !file.isDirectory()) {
                         appConfig.setLastUseDir(file.getParentFile());
                         CryptUtils.erase(file);
                     }
 
                 } catch (Exception ex) {
                     ErrorMessageHelper.showErrorDialog(this, ex);
                 }
             }
             fileTreePanel.refresh();
         }
     }
 
     /**
      * ファイルの削除
      */
     protected void onDelete(File file) {
         if (file == null || file.isDirectory()) {
             // ファイルが指定されていないか、ファイルでなければスキップする.
             return;
         }
 
         String title = resource.getString("confirm.title");
         String messageTmpl = resource.getString("confirm.erase.file");
         String message = String.format(messageTmpl, file.getName());
 
         int ret = JOptionPane.showConfirmDialog(this, message, title,
                 JOptionPane.YES_NO_OPTION);
         if (ret == JOptionPane.YES_OPTION) {
             try {
                 // ランダム値で埋めてからファイルエントリを削除する.
                 CryptUtils.erase(file);
 
                 // ファイル一覧を更新する.
                 fileTreePanel.refresh();
 
             } catch (Exception ex) {
                 ErrorMessageHelper.showErrorDialog(this, ex);
             }
         }
     }
 
     /**
      * メインフレームを破棄する場合
      */
     protected void onClosing() {
         // クリップボードをクリアする.
         try {
             Toolkit tk = Toolkit.getDefaultToolkit();
             Clipboard cb = tk.getSystemClipboard();
             cb.setContents(new StringSelection(""), null);
 
         } catch (Exception ex) {
             logger.log(Level.WARNING, "cannot clear clipboard.", ex);
         }
 
         // 変更されていない子ウィンドウがあるか検査する.
         boolean needConfirm = false;
         for (JInternalFrame child : desktop.getAllFrames()) {
             if (!child.isDisplayable() || !child.isVisible()) {
                 // 表示できないか、表示されていないものは除外する.
                 continue;
             }
             if (child instanceof DocumentInternalFrame) {
                 DocumentInternalFrame c = (DocumentInternalFrame) child;
                 if (c.isModified()) {
                     // 未保存のドキュメントがあるので確認が必要.
                     needConfirm = true;
                     break;
                 }
             }
         }
 
         // 変更を破棄してよいか確認する.
         if (needConfirm) {
             String message = resource.getString("confirm.close.unsavedchanges");
             String title = resource.getString("confirm.title");
             int ret = JOptionPane.showConfirmDialog(this, message, title,
                     JOptionPane.YES_NO_OPTION);
             if (ret != JOptionPane.YES_OPTION) {
                 // まだ閉じない.
                 return;
             }
         }
 
         // メインフレームを破棄する.
         dispose();
         logger.log(Level.INFO, "disposed mainframe");
     }
 }
