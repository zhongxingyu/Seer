 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 
 public class Restaurant_v21_gui extends JFrame implements ActionListener {
 
 	JTabbedPane content;
 	JButton gen;
 	JTextArea text;
 	GradingPanel Dsign, host, customer, wait, cook, cash, mark, ani, git, deduc;
 	GradingPanel v2;
 	public Restaurant_v21_gui() {
 		setVisible(true);
 		setResizable(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		content = new JTabbedPane();
 
 		setUpV2();
 		setUpDesign();
 
 		setUpHost();
 		setUpCust();
 		setUpWait();
 		setUpCook();
 		setUpCash();
 		setUpMark();
 
 		setUpAnim();
 		setUpGit();
 		setUpDedu();
 
 		setUpFinal();
 
 		add(content);
 		pack();
 	}
 
 	private void populateTab(Item[] items, GradingPanel panel) {
 		for (Item i:items)
 			panel.addItem(i);
 
 		content.addTab(panel.ID, panel);
 	}
 
 	private void setUpV2() {
 
 		Item V2[] = new Item[5];
 		V2[0] = new Item(2, "One Customer, One Waiter", "V2 Requirements");
 		V2[1] = new Item(2, "Multiple Customer, One Waiter", "V2 Requirements");
 		V2[2] = new Item(2, "Load the Tables, One Waiter", "V2 Requirements");
 		V2[3] = new Item(2, "Multiple Customer, Multiple Waiter", "V2 Requirements");
 		V2[4] = new Item(2, "Functional Pause Button", "V2 Requirements");
 
 		v2 = new GradingPanel("V2 Requirements");
 		populateTab(V2, v2);
 	}
 
 	private void setUpDesign() {
 
 		Item Design[] = new Item[14];
 		Design[0] = new Item(1, "Normative Interaction Diagram", "Design");
 		Design[1] = new Item(1, "Interaction Diagram includes stuff", "Design");
 		Design[2] = new Item(2, "Host - Full Design of Host", "Host Design");
 		Design[3] = new Item(1, "Host - Good Pseudocode", "Host Design");
 		Design[4] = new Item(2, "Cook - Full Design of Cook", "Cook Design");
 		Design[5] = new Item(1, "Cook - Good Pseudocode", "Cook Design");
 		Design[6] = new Item(2, "Customer - Full Design of Customer", "Customer Design");
 		Design[7] = new Item(1, "Customer - Good Pseudocode", "Customer Design");
 		Design[8] = new Item(2, "Waiter - Full Design of Waiter", "Waiter Design");
 		Design[9] = new Item(1, "Waiter - Good Pseudocode", "Waiter Design");
 		Design[10] = new Item(2, "Cashier - Full Design of Cashier", "Cashier Design");
 		Design[11] = new Item(1, "Cashier - Good Pseudocode", "Cashier Design");
 		Design[12] = new Item(2, "Market - Full Design of Market", "Market Design");
 		Design[13] = new Item(1, "Market - Good Pseudocode", "Market Design");
 
 		Dsign = new GradingPanel("Design");
 		populateTab(Design, Dsign);
 	}
 
 	private void setUpHost() {
 		Item Host[] = new Item[1];
 		Host[0] = new Item(3, "Host correctly gives/denies breaks", "Host");
 
 
 		host = new GradingPanel("Host");
 
 		for (Item i:Host)
 			host.addItem(i);
 
 		content.addTab("Host", host);
 	}
 
 	private void setUpCust() {
 		Item Cust[] = new Item[8];
 		Cust[0] = new Item(2, "Reorders when original order is out of stock", "Cust");
 		Cust[1] = new Item(2, "Pays cashier after receiving check", "Cust");
 		Cust[2] = new Item(2, "Some customers choose to leave rather than wait when the restaurant is full", "Cust");
 		Cust[3] = new Item(1, "Customers have a way of keeping track of their money", "Cust");
 		Cust[4] = new Item(1, "Customers who have no money to order anything sometimes choose to leave", "Cust");
 		Cust[5] = new Item(1, "Customers who have no money to order anything sometimes<br>chooses to order anyway and ends up without enough money to pay", "Cust");
 		Cust[6] = new Item(2, "Customers who have only enough money to order the cheapest item will only order that cheap item", "Cust");
 		Cust[7] = new Item(2, "Customers who have only enough money to order the cheapest item will leave if that item is out of stock", "Cust");
 
 		customer = new GradingPanel("Customer");
 
 		for (Item i:Cust)
 			customer.addItem(i);
 
 		content.addTab("Customer", customer);
 	}
 
 	private void setUpWait() {
 		Item Wait[] = new Item[10];
 		Wait[0] = new Item(1, "Asks host if break can be taken", "Wait");
 		Wait[1] = new Item(2, "Break can be either accepted or denied", "Wait");
 		Wait[2] = new Item(1, "Goes on break", "Wait");
 		Wait[3] = new Item(2, "Correctly returns to work", "Wait");
 		Wait[4] = new Item(1, "Gets notified when cook is out of food", "Wait");
 		Wait[5] = new Item(1, "Tells customer when Cook is out of food", "Wait");
 		Wait[6] = new Item(1, "Takes customer's order again", "Wait");
 		Wait[7] = new Item(1, "Asks cashier to compute check", "Wait");
 		Wait[8] = new Item(1, "Receives check from cashier", "Wait");
 		Wait[9] = new Item(1, "Gives check to the customer", "Wait");
 
 		wait = new GradingPanel("Waiter");
 
 		for (Item i:Wait)
 			wait.addItem(i);
 
 		content.addTab("Waiter", wait);
 	}
 
 	private void setUpCook() {
 		Item Cook[] = new Item[4];
 		Cook[0] = new Item(1, "Checks when inventory is low", "Cook");
 		Cook[1] = new Item(2, "Orders food from market", "Cook");
 		Cook[2] = new Item(1, "Tells waiter customer must reorder", "Cook");
 		Cook[3] = new Item(2, "Has multiple (at least 3) markets to order from", "Cook");
 
 		cook = new GradingPanel("Cook");
 
 		for (Item i:Cook)
 			cook.addItem(i);
 
 		content.addTab("Cook", cook);
 	}
 
 	private void setUpCash() {
 		Item Cash[] = new Item[5];
 		Cash[0] = new Item(2, "Receives order from waiter at some point", "Cash");
 		Cash[1] = new Item(2, "Creates check", "Cash");
 		Cash[2] = new Item(2, "Gives the waiter the check upon request", "Cash");
 		Cash[3] = new Item(2, "Lets customer pay the next time he comes to the restaurant", "Cash");
 		Cash[4] = new Item(2, "Receives money from the customer", "Cash");
 
 		cash = new GradingPanel("Cash");
 
 		for (Item i:Cash)
 			cash.addItem(i);
 
		content.addTab("Cash", cash);
 
 	}
 
 	private void setUpMark() {
 		Item Mark[] = new Item[6];
 		Mark[0] = new Item(1, "Keeps track of inventory", "Mark");
 		Mark[1] = new Item(2, "Receives orders from Cook", "Mark");
 		Mark[2] = new Item(2, "Fulfills Cook's order", "Mark");
 		Mark[3] = new Item(2, "Can run out of food", "Mark");
 		Mark[4] = new Item(2, "Does not restock", "Mark");
 		Mark[5] = new Item(2, "Tells Cook when order cannot be fulfilled", "Mark");
 
 		mark = new GradingPanel("Mark");
 
 		for (Item i:Mark)
 			mark.addItem(i);
 
 		content.addTab("Market", mark);
 
 	}
 
 	private void setUpAnim() {
 		Item Anim[] = new Item[4];
 		Anim[0] = new Item(2, "Functional Waiter On Break button that ensures<br>there is at least one waiter working at all times", "Animation");
 		Anim[1] = new Item(3, "No Customer Money: Can't afford anything on the menu. Customer leaves", "Animation");
 		Anim[2] = new Item(3, "No Customer Money: Restaurant runs out of Customer order, can't afford anything else", "Animation");
 		Anim[3] = new Item(2, "Animation support for Customer leaving after restaurant runs out of your choice", "Animation");
 
 		ani = new GradingPanel("Animation");
 
 		for (Item i:Anim)
 			ani.addItem(i);
 
 		content.addTab("Animation", ani);
 	}
 
 	private void setUpGit() {
 		Item Git[] = new Item[5];
 		Git[0] = new Item(1, "Intermediate commits for the assignment showing progress (at least 5 commits)", "Git");
 		Git[1] = new Item(1, "Final marked commit for submission/grading", "Git");
 		Git[2] = new Item(1, "Commits - in general - have appropriate, descriptive names", "Git");
 		Git[3] = new Item(2, "README.md contains stuff", "Git");
 		Git[4] = new Item(-5, "Violations of what to do & what not to do in your repository", "Git");
 
 		git = new GradingPanel("Git");
 
 		for (Item i:Git)
 			git.addItem(i);
 
 		content.addTab("Git", git);
 	}
 
 	private void setUpDedu() {
 		Item Dedu[] = new Item[5];
 		Dedu[0] = new Item(-10, "Not using the agent methodology correctly", "Deductions");
 		Dedu[1] = new Item(-2, "Runtime errors other than concurrent modification errors, which we ignore for now", "Deductions");
 		Dedu[2] = new Item(-10, "Missing a lab deadline (for each lab deadline missed)", "Deductions");
 		Dedu[3] = new Item(-5, "GUI is not reasonably sized (components cut off, text unreadable, etc.)", "Deductions");
 		Dedu[4] = new Item(-10, "Program only runs on student's machine", "Deductions");
 
 		deduc = new GradingPanel("Deductions");
 
 		for (Item i:Dedu)
 			deduc.addItem(i);
 
 		content.addTab("Deductions", deduc);
 	}
 
 	private void setUpFinal() {
 		JPanel fin = new JPanel();
 		fin.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		gen = new JButton("Generate Report");
 		gen.addActionListener(this);
 		text = new JTextArea();
 
 		c.weightx = 1;
 		fin.add(gen,c);
 		c.gridy = 2;
 		c.weighty = 1;
 		fin.add(text,c);
 		content.addTab("Finish", fin);
 	}
 
 	public int getTotal() {
 		return Dsign.addItems()
 				+ host.addItems() + customer.addItems() + wait.addItems() + cook.addItems() + cash.addItems() + mark.addItems()
 				+ ani.addItems() + v2.addItems() + git.addItems() + deduc.addItems();
 	}
 
 	public static void main(String[] args) {
 		Restaurant_v21_gui test = new Restaurant_v21_gui();
 		test.setSize(800, test.getHeight());
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(gen)) {
 			String output = "Your Assignment v2.1 Submission Received " + getTotal() + " points. The following error(s) were encountered:\n";
 			output += " 1. Milestone.v2.1A - " + (10-v2.addItems()) + " points were deducted\n";
 			output += v2.getErrors();
 			output += "\n";
 			output += " 2. Milestone.v2.1B - " + (20-Dsign.addItems()) + " points were deducted\n";
 			output += Dsign.getErrors();
 			output += "\n";
 			output += " 3. Milestone.v2.1C - " + (55-host.addItems()-customer.addItems()-wait.addItems()-cook.addItems()-cash.addItems()-mark.addItems()) + " points were deducted\n";
 			output += host.getErrors() + customer.getErrors() + wait.getErrors() + cook.getErrors() + cash.getErrors() + mark.getErrors();
 			output += "\n";
 			output += " 4. Milestone.v2.1D - " + (10-ani.addItems()) + " points were deducted\n";
 			output += ani.getErrors();
 			output += "\n";
 			output += " 5. Git Usage - " + (5-git.addItems()) + " points were deducted\n";
 			output += git.getErrors();
 			output += "\n";
 			if (deduc.addItems() != 0) {
 				output += "Also, the following deductions were applied\n";
 				output += deduc.getErrors();
 				output += "\n";
 			}
 			output += "Other Notes:\n\n";
 			output += ">Although you may be getting this message via email, it is an issue on issue on your private GitHub repository and you should interact with it on GitHub\n\n"
 					+ ">Any and all discussions pertaining to this deduction **must** be done via comments on this issue. If you are statisfied with the grading, you **must** close the issue. Note that a closed issue implicitly signals that you do not want to discuss this any further. No emails are accepted.\n\n"
 					+ ">You have until Oct 29th, 2013 to dispute this deduction. Please allow up to 72 hours for a response to a regrade request. You are expected to follow the _Grading Disputes_ policies outlined in the [course syllabus](http://www-scf.usc.edu/~csci201/syllabus.html).\n\n";
 			System.out.println(output);
 			output.replaceAll("\n","<br />");
 			text.setText(output);
 
 		}
 	}
 
 }
