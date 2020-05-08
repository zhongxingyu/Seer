 package org.zaproxy.zap.extension.saml;
 
 import org.apache.commons.httpclient.HttpsURL;
 import org.apache.commons.httpclient.URIException;
 import org.parosproxy.paros.network.HtmlParameter;
 import org.parosproxy.paros.network.HttpMalformedHeaderException;
 import org.parosproxy.paros.network.HttpMessage;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Map;
 
 public class SAMLRequestEditor{
 
     //The UI components
     private JPanel samlEditorPanel;                     //the root panel of the Jframe
 
     private JTabbedPane tabbedPane1RequestResponse;     //Tabbed pane for request and response
     private JPanel requestPanel;                        //The panel to display the components of request
     private JPanel responsePanel;                       //The panel to display the components of the response
 
     private JSplitPane responseSplitPane;               //Split pane to divide response head and body
     private JScrollPane responseHeaderScrollPane;
     private JScrollPane responseBodyScrollPane;
     private JTextArea responseHeaderTextArea;           //Text area to display the http headers of the response
     private JTextArea responseBodyTextArea;             //Text area to display the http response body
 
     private JPanel attribPanel;                         //The panel to display the saml attribute name/value pairs
     private JScrollPane reqAttribScrollPane;            //Scroll pane to give the scrollability to attrib panel
 
     private JTextArea samlMsgTxtArea;                   //The text area to display the decoded saml message
     private JScrollPane samlMsgScrollPane;              //Scroll pane to give the scrollability to saml msg text area
     private JLabel lblWarningMsg;                       //Label to show the warning text
 
     private JPanel footerPanel;                         //Panel to hold the items like buttons
     private JButton resendButton;                       //Button to resend the request
     private JButton resetButton;                        //Button to reset the request items
 
     //Other variables
     private HttpMessage httpMessage;
     private SAMLMessage samlMessage;                    //Representation of the saml message
     private String samlParameter;                       //"SAMLRequest" or "SAMLResponse"
     private String relayState;                          //Relay state parameter
     private Binding samlBinding;                        //Whether the http message has used http redirect or post
 
     public SAMLRequestEditor(HttpMessage message){
         this.httpMessage = message;
         setMessage();
     }
 
     /**
      * Initialize UI components and layouts
      */
     protected void initUIComponents(){
         samlEditorPanel = new JPanel();
 
         tabbedPane1RequestResponse = new JTabbedPane();
         requestPanel = new JPanel();
         responsePanel = new JPanel();
 
         responseSplitPane = new JSplitPane();
         responseBodyTextArea = new JTextArea();
         responseHeaderTextArea = new JTextArea();
         responseHeaderScrollPane = new JScrollPane();
         responseBodyScrollPane = new JScrollPane();
 
         reqAttribScrollPane = new JScrollPane();
 
         samlMsgTxtArea = new JTextArea();
         samlMsgScrollPane = new JScrollPane();
         lblWarningMsg = new JLabel();
 
         footerPanel = new JPanel();
         resendButton = new JButton();
         resetButton = new JButton();
     }
 
     /**
      * Do the layout of the components in the frame
      */
     private void doLayout(){
         samlEditorPanel.setLayout(new BorderLayout());
         samlEditorPanel.add(tabbedPane1RequestResponse);
         tabbedPane1RequestResponse.add("Request",requestPanel);
         tabbedPane1RequestResponse.add("Response",responsePanel);
 
         requestPanel.setLayout(new BorderLayout());
         samlMsgScrollPane.setViewportView(samlMsgTxtArea);
         requestPanel.add(samlMsgScrollPane, BorderLayout.PAGE_START);
 
         initSAMLContents();    //Initialize the layout of the saml attributes
         requestPanel.add(reqAttribScrollPane,BorderLayout.CENTER);
 
         //Footer
         footerPanel.setLayout(new java.awt.GridLayout(2, 1));
         lblWarningMsg.setText("Note : This add-on would only run very basic test cases for SAML implementations. " +
                 "Signed SAML assertions cannot be tampered with at this time because the signing keys have not been " +
                 "made available to ZAP");
         JSplitPane buttonSplitPane = new JSplitPane();
         buttonSplitPane.setDividerSize(0);
         buttonSplitPane.setLeftComponent(resendButton);
         buttonSplitPane.setRightComponent(resetButton);
         buttonSplitPane.setResizeWeight(0.5);
         resendButton.setText("Resend");
         resetButton.setText("Reset");
         footerPanel.add(buttonSplitPane);
         footerPanel.add(lblWarningMsg);
         requestPanel.add(footerPanel,BorderLayout.PAGE_END);
 
         //Response panel
         responsePanel.setLayout(new BorderLayout());
         responsePanel.add(responseSplitPane);
         responseHeaderScrollPane.setViewportView(responseHeaderTextArea);
         responseBodyScrollPane.setViewportView(responseBodyTextArea);
         responseSplitPane.setTopComponent(responseHeaderScrollPane);
         responseSplitPane.setBottomComponent(responseBodyScrollPane);
         responseSplitPane.setResizeWeight(0.5);
         responseSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
     }
 
     /**
      * Initialize the SAML contents. i.e. message and the attributes
      */
     private void initSAMLContents(){
         initSAMLTextArea();
         initSAMLAttributes();
     }
 
     /**
      * Initialize the saml message text area
      */
     private void initSAMLTextArea(){
         try {
             samlMsgTxtArea.setText(samlMessage.getPrettyFormattedMessage());
             samlMsgTxtArea.addFocusListener(new FocusListener() {
                 @Override
                 public void focusGained(FocusEvent e) {
                 }
 
                 @Override
                 public void focusLost(FocusEvent e) {
                     samlMessage = new SAMLMessage(samlMsgTxtArea.getText(),samlMessage.getSamlParameter());
                     initSAMLAttributes();
                 }
             });
         } catch (SAMLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Initialize the SAML attributes (label, value pairs)
      */
     private void initSAMLAttributes(){
         attribPanel = new JPanel();
         attribPanel.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
         Map<String, String> samlAttributes = samlMessage.getAttributeMapping();
 
         for (final Map.Entry<String, String> entry : samlAttributes.entrySet()) {
             JSplitPane sPane = new JSplitPane();
             JLabel lbl = new JLabel();
             final JTextField txtValue = new JTextField();
 
             sPane.setDividerLocation(300);
             sPane.setDividerSize(0);
 
             lbl.setText(entry.getKey());
             sPane.setLeftComponent(lbl);
 
             txtValue.setText(entry.getValue());
             sPane.setRightComponent(txtValue);
 
             //update the saml message on attribute value changes
             txtValue.addFocusListener(new FocusListener() {
                 @Override
                 public void focusGained(FocusEvent e) {
                 }
 
                 @Override
                 public void focusLost(FocusEvent e) {
                     try {
                         samlMessage.setValueTo(entry.getKey(),txtValue.getText());
                         samlMsgTxtArea.setText(samlMessage.getPrettyFormattedMessage());
                     } catch (SAMLException e1) {
                        JOptionPane.showMessageDialog(reqAttribScrollPane,e1.getMessage(),"Error in value",JOptionPane.ERROR_MESSAGE);
                     }
                 }
             });
             attribPanel.add(sPane);
         }
         reqAttribScrollPane.setViewportView(attribPanel);
     }
 
     private void initButtons(){
         resendButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent event) {
                 try {
                     SAMLResender.buildSAMLRequest(httpMessage,samlParameter,samlMessage.getPrettyFormattedMessage(),samlBinding);
                     SAMLResender.resendMessage(httpMessage);
                     updateResponse(httpMessage);
                 } catch (SAMLException e) {
                 }
             }
         });
         resetButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 samlMessage.resetMessage();
                 initSAMLContents();
             }
         });
     }
 
     private void updateResponse(HttpMessage msg){
         responseBodyTextArea.setText(msg.getResponseBody().createCachedString("UTF-8"));
         responseHeaderTextArea.setText(msg.getResponseHeader().toString());
         tabbedPane1RequestResponse.setSelectedIndex(1);
 
     }
 
     private void setMessage(){
         for (HtmlParameter urlParameter : httpMessage.getUrlParams()) {
             if(urlParameter.getName().equals("SAMLRequest")||urlParameter.getName().equals("SAMLResponse")){
                 String msgString = extractSAMLMessage(urlParameter.getValue(), Binding.HTTPRedirect);
                 samlMessage = new SAMLMessage(msgString,urlParameter.getName());
                 samlBinding = Binding.HTTPRedirect;
                 samlParameter = urlParameter.getName();
             } else if(urlParameter.getName().equals("RelayState")){
                 relayState = urlParameter.getValue();
             }
         }
 
         for (HtmlParameter formParameter : httpMessage.getFormParams()) {
             if(formParameter.getName().equals("SAMLRequest")||formParameter.getName().equals("SAMLResponse")){
                 String msgString = extractSAMLMessage(formParameter.getValue(), Binding.HTTPPost);
                 samlMessage = new SAMLMessage(msgString,formParameter.getName());
                 samlBinding = Binding.HTTPPost;
                 samlParameter = formParameter.getName();
             }else if(formParameter.getName().equals("RelayState")){
                 relayState = formParameter.getValue();
             }
         }
     }
 
     /**
      * Decode the SAML messages based on the binding used
      * @param val the SAML message to decode
      * @param binding The binding used
      * @return The decoded SAML message if success, or the original string if failed
      */
     private String extractSAMLMessage(String val, Binding binding){
         try {
             switch (binding) {
                 case HTTPPost:
                     val = URLDecoder.decode(val,"UTF-8");
                 case HTTPRedirect:
                     byte[] b64decoded = SAMLUtils.b64Decode(val);
                     return SAMLUtils.inflateMessage(b64decoded);
                 default:
                     break;
             }
         } catch (UnsupportedEncodingException e) {
 
         } catch (SAMLException e) {
 
         }
         return val;
     }
 
 
     /**
      * Shows the extension UI
      */
     public void showUI(){
         JFrame frame = new JFrame("SAML Request editor");
         frame.setSize(800,700);
         frame.setLayout(new BorderLayout());
         initUIComponents();
         doLayout();
         initButtons();
         frame.setContentPane(samlEditorPanel);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
     }
 
     public static void main(String[] args) throws URIException, HttpMalformedHeaderException {
         HttpMessage msg= new HttpMessage(new HttpsURL("https://localhost:9443/samlsso?SAMLRequest=jZNPb%2BIwEMW%2FiuV7SPgnwCJUlKpapO42S9I99OaaoVhy7KxnQtlvv05CdjlUqFfP85t5v7GXd%2BfSsBN41M6mfDhIOAOr3F7b95S%2FFI%2FRnN%2BtlihLU4l1TUe7g981ILFwz6JoCymvvRVOokZhZQkoSIl8%2Ff1JjAaJqLwjp5zhbI0InkKjjbNYl%2BBz8Cet4GX3lPIjUSXi2DglzdEhiXkyT%2BI8f85lWRlYV1WM6Ix71zY4EXn9VhN0TmHYi9XW7uEccozGi9FkupgsOHt0XkE7esoP0iBwtn1IeQi6xUwi6hP8LyDWwQNJWkr5KBmOo2QWJeNiuBCjmZgkg%2Bls%2BspZdsl0r21H6haAt06E4ltRZFH2nBec%2FeqJBwHv%2Bbbd%2FdfJyp4nX11zWsbXdr35j3B9%2B5A5o9UftjbGfWw8SArZydfQYiol3W7YnOh9dGilomoyIIElzvKs8f9ZS6MPGnzKu%2BY87ttfng3s202EtRGciW1cWUmvsSEBZ6nowkJcqzYmBN3B4QrMl7nclCmhGutw3LyDD%2Bf3zV5BhSkLLy1WzlMH89N5Vj3oT7P9q17%2FmtVf&RelayState=null"));
         SAMLRequestEditor requestEditor = new SAMLRequestEditor(msg);
         requestEditor.showUI();
     }
 }
