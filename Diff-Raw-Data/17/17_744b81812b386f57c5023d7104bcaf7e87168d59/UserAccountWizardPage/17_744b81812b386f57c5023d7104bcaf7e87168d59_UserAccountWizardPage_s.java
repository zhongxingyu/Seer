 package de.objectcode.time4u.client.connection.ui.wizards;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import de.objectcode.time4u.client.connection.api.ConnectionFactory;
 import de.objectcode.time4u.client.connection.api.IConnection;
 import de.objectcode.time4u.client.store.api.RepositoryFactory;
 import de.objectcode.time4u.server.api.data.EntityType;
 import de.objectcode.time4u.server.api.data.Person;
 import de.objectcode.time4u.server.api.data.ServerConnection;
 
 public class UserAccountWizardPage extends WizardPage
 {
   ServerConnection m_serverConnection;
 
   Text m_userIdText;
   Text m_passwordText;
   Text m_passwordConfirmText;
   Button m_testButton;
   Label m_testResultLabel;
 
   boolean m_active;
 
   public UserAccountWizardPage(final ServerConnection serverConnection)
   {
     super("UserAccount", "Account Information", null);
     m_serverConnection = serverConnection;
   }
 
   public void createControl(final Composite parent)
   {
     final Composite root = new Composite(parent, SWT.NONE);
     root.setLayout(new GridLayout(2, false));
     root.setLayoutData(new GridData(GridData.FILL_BOTH));
     setControl(root);
 
     final Label userIdLabel = new Label(root, SWT.NONE);
     userIdLabel.setText("User");
     m_userIdText = new Text(root, SWT.BORDER);
     m_userIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
     m_userIdText.setTextLimit(30);
     m_userIdText.setText(m_serverConnection.getCredentials() != null
         && m_serverConnection.getCredentials().get("userId") != null ? m_serverConnection.getCredentials()
         .get("userId") : "");
     m_userIdText.addKeyListener(new KeyAdapter() {
       @Override
       public void keyReleased(final KeyEvent e)
       {
         enableTestButton();
       }
     });
 
     final Label passwordLabel = new Label(root, SWT.NONE);
     passwordLabel.setText("Password");
     m_passwordText = new Text(root, SWT.BORDER | SWT.PASSWORD);
     m_passwordText.setTextLimit(30);
     m_passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
     m_passwordText.addKeyListener(new KeyAdapter() {
       @Override
       public void keyReleased(final KeyEvent e)
       {
         enableTestButton();
       }
     });
 
     final Label passwordConfirmLabel = new Label(root, SWT.NONE);
     passwordConfirmLabel.setText("Password Confirm");
     m_passwordConfirmText = new Text(root, SWT.BORDER | SWT.PASSWORD);
     m_passwordConfirmText.setTextLimit(30);
     m_passwordConfirmText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
     m_passwordConfirmText.addKeyListener(new KeyAdapter() {
       @Override
       public void keyReleased(final KeyEvent e)
       {
         enableTestButton();
       }

     });
 
     m_testButton = new Button(root, SWT.PUSH);
     m_testButton.setText("Test");
     GridData gridData = new GridData();
     gridData.horizontalAlignment = GridData.CENTER;
     gridData.horizontalSpan = 2;
     m_testButton.setLayoutData(gridData);
     m_testButton.setEnabled(false);
     m_testButton.addSelectionListener(new SelectionAdapter() {
       @Override
       public void widgetSelected(final SelectionEvent e)
       {
         testAccount();
       }
     });
 
     m_testResultLabel = new Label(root, SWT.CENTER);
     m_testResultLabel.setText("");
     gridData = new GridData(GridData.FILL_HORIZONTAL);
     gridData.horizontalSpan = 2;
     m_testResultLabel.setLayoutData(gridData);
 
     setPageComplete(false);
   }
 
   protected void enableTestButton()
   {
     boolean enabled = true;
 
     enabled = enabled && m_userIdText.getText() != null && m_userIdText.getText().length() > 0;
     enabled = enabled && m_passwordText.getText() != null && m_passwordText.getText().length() > 0;
     enabled = enabled && m_passwordText.getText().equals(m_passwordConfirmText.getText());
 
     m_testButton.setEnabled(enabled);
   }
 
   protected void testAccount()
   {
     if (m_serverConnection.getCredentials() == null) {
       m_serverConnection.setCredentials(new HashMap<String, String>());
     }
     m_serverConnection.getCredentials().put("userId", m_userIdText.getText());
     m_serverConnection.getCredentials().put("password", m_passwordText.getText());
 
     try {
       final IConnection connection = ConnectionFactory.openConnection(m_serverConnection);
       if (!connection.checkLogin(m_serverConnection.getCredentials())) {
         if (connection.registerLogin(m_serverConnection.getCredentials())) {
           m_testResultLabel.setText("New account registered");
           m_testResultLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GREEN));
           setPageComplete(true);
           return;
         } else {
           m_testResultLabel.setText("Failed to register new account");
           m_testResultLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
           setPageComplete(false);
           return;
         }
       } else {
         final Person person = connection.getPerson();
 
         if (!RepositoryFactory.getRepository().getOwner().getId().equals(person.getId())) {
           // If the repository is empty we assume that the user wants to connect to the existing data on the server
           boolean cleanRepository = true;
           final Map<EntityType, Long> status = RepositoryFactory.getRepository().getRevisionStatus();
 
           for (final EntityType type : EntityType.values()) {
             if (type == EntityType.PERSON) {
               if (status.get(type).longValue() != 2L) {
                 cleanRepository = false;
                 break;
               }
             } else if (status.get(type).longValue() != 0L) {
               cleanRepository = false;
               break;
             }
           }
           if (cleanRepository) {
             RepositoryFactory.getRepository().changeOwnerId(person.getId());
             m_serverConnection.setMappedPersonId(null);
           } else {
             m_serverConnection.setMappedPersonId(person.getId());
           }
         }
 
         if (connection.registerClient()) {
           m_testResultLabel.setText("Registered client");
           m_testResultLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GREEN));
           setPageComplete(true);
           return;
         } else {
           m_testResultLabel.setText("Client register failed");
           m_testResultLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
           setPageComplete(false);
           return;
         }
       }
     } catch (final Exception e) {
       e.printStackTrace();
       m_testResultLabel.setText("Connection failed: " + e.getMessage());
       m_testResultLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
       setPageComplete(false);
     }
   }
 
   @Override
   public void setVisible(final boolean visible)
   {
     if (visible) {
       m_active = true;
     } else {
       if (m_active) {
         if (m_serverConnection.getCredentials() == null) {
           m_serverConnection.setCredentials(new HashMap<String, String>());
         }
         m_serverConnection.getCredentials().put("userId", m_userIdText.getText());
         m_serverConnection.getCredentials().put("password", m_passwordText.getText());
 
       }
       m_active = false;
     }
     super.setVisible(visible);
   }
 }
