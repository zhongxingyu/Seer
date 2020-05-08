 /*
 * Copyright (c) 2010, 2011, 2015 Thomas Kuehne <thomas@kuehne.cn>
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package cn.kuehne.kinaseblender;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 
 /**
  * Display contact and copyright notice. 
  */
 final class About implements ActionListener {
 	private final JButton button;
 
 	About(final JButton b) {
 		button = b;
 		button.addActionListener(this);
 	}
 
 	public void actionPerformed(final ActionEvent e) {
 		JOptionPane
 				.showMessageDialog(
 						button,
 						"<html><head><title>About Kinaseblender</title></head><body><center><big><b>Kinaseblender</b></big></center><br>"
 								+ "Minimize number of sources against number of products.<br><br>"
								+ "Copyright (c) 2010, 2011, 2012, 2015 Thomas Kuehne &#60;thomas@kuehne.cn&#62;<br>"
 								+ "<br>"
 								+ "<font size='-1'>Permission is hereby granted, free of charge, to any person obtaining<br>"
 								+ "a copy of this software and associated documentation files (the<br>"
 								+ "\"Software\"), to deal in the Software without restriction, including<br>"
 								+ "without limitation the rights to use, copy, modify, merge, publish,<br>"
 								+ "distribute, sublicense, and/or sell copies of the Software, and to<br>"
 								+ "permit persons to whom the Software is furnished to do so, subject to<br>"
 								+ "the following conditions:<br>"
 								+ "<br>"
 								+ "The above copyright notice and this permission notice shall be<br>"
 								+ "included in all copies or substantial portions of the Software.<br>"
 								+ "<br>"
 								+ "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND,<br>"
 								+ "EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF<br>"
 								+ "MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT<br>"
 								+ "IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY<br>"
 								+ "CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,<br>"
 								+ "TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE<br>"
 								+ "SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE."
 								+ "</font></body></html>",
 						"About Kinaseblender", JOptionPane.INFORMATION_MESSAGE,
 						AppFrame.getIcon());
 	}
}
