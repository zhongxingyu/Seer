 import java.io.*;
 import java.net.*;
 
 public class MLPlayerAlphaTwo
 {
 	// If debug should be printed or not.
 	private boolean PRINT_DEBUG = false;
 
 	// The constants that define the reinforcement learning algorithm.
 	double epsilon = 0.15f; // FOR TESTING!
 	double eta = 0.25f;
 	double gamma = 0.99f;
 	double lambda = 0.5f;
 
 	// An internal board representation.
 	private int internal_board[][] = null;
 
 	// The player.
 	Player thePlayer;
 
 	// The player id of this player.
 	int player_id = 0;
 
 	// The eligibility trace variables.
 	double[] game_history = new double[11*10 + 1];
 	int game_history_length = 0;
 
 	// Constructor.
 	public MLPlayerAlphaTwo(Player p)
 	{
 		thePlayer = p;
 	}
 
 	// The play function, which connects to the socket and plays.
 	public void play(int my_id) throws IOException,ClassNotFoundException
 	{
 		// Open socket and in/out streams.
 		Socket sock = new Socket("localhost", Player.getSocketNumber(thePlayer));
 		ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
 		ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
 		BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
 
 		Weights weights = new Weights("alpha_weights.txt");
 
 		// Create the weights file if it doesn't exist and initialize to default values.
 		File weights_file = new File("alpha_weights.txt");
 		if (!weights_file.exists())
 		{
 			System.out.println("Weights file does not exist. Creating one with default weights");
 			weights_file.createNewFile();
 			double[] initialWeights = new double[FeatureExplorer.getNumFeatures()];
 			for(int x = 0; x < FeatureExplorer.getNumFeatures(); x++)
 			{
 				double k = 1.0f / Math.sqrt((double)FeatureExplorer.getNumFeatures());
 				initialWeights[x] = 2.0f * Math.random() * k - k;
 			}
 			weights.setWeights(initialWeights);
 			weights.saveWeights();
 		}
 
 		// Set the id.
 		player_id = my_id;
 
 		// Get the game rules.
 		Rules gameRules = (Rules)in.readObject();
 		if (PRINT_DEBUG) System.out.printf("Num Rows: %d, Num Cols: %d, Num Connect: %d\n", gameRules.numRows, gameRules.numCols, gameRules.numConnect);
 
 		// Create the internal board.
 		internal_board = new int[gameRules.numRows][gameRules.numCols];
 		for (int r = 0; r < gameRules.numRows; r++)
 			for (int c = 0; c < gameRules.numCols; c++)
 				internal_board[r][c] = 0;
 
 		// Start playing the game, first by waiting fo the initial message.
 		if (PRINT_DEBUG) System.out.println("Waiting...");
 		GameMessage mess = (GameMessage)in.readObject();
 
 		// The main game loop.
 		double approx_qsa = -1.0f;
 		double y_i = 0.0f; // For the current board (state i+1).
 		double[] x_i = new double[FeatureExplorer.getNumFeatures()]; // For the previous board after you took an action AND opponent took action (state i).
 		double[] x_ip1 = new double[FeatureExplorer.getNumFeatures()]; // For the current board after you took an action AND oppoent took action (state i+1).
 		boolean beginning = true;
 		int move = 0;
 		int selected_column = 0;
 		while(mess.win == Player.EMPTY)
 		{
 			// Print some debug.
 			if (PRINT_DEBUG) weights.printWeights();
 
 			// If the first message is not the begin message (-1), then record what the other player did.
 			if(mess.move != -1)
 			{
 				int r = 0;
 				for (r = 0; r < gameRules.numRows; r++)
 				{
 					if (internal_board[r][mess.move] != 0)
 					{
 						if (r == 0) break;
 						internal_board[r - 1][mess.move] = player_id % 2 + 1;
 						break;
 					}
 				}
 				if (r > 0 && r == gameRules.numRows) internal_board[r - 1][mess.move] = player_id % 2 + 1;
 				if (r == 0) System.out.println("Alpha has detected an improper move made by the other player.");
 			}
 			else
 			{
 				// Randomly move the first time if you are first.
 				mess.move = (int)((float)gameRules.numCols * Math.random());
 
 				// Create the features for the blank board.
 				FeatureExplorer ff = new FeatureExplorer();
 				ff.initialize(internal_board, gameRules.numRows, gameRules.numCols, -1, player_id);
 				x_ip1 = ff.getFeatures(); // Previous board given that we chose this action.
 
 				// Update internal representaiton and tell server about the move (e.g. take action).
 				internal_board[gameRules.numRows - 1][mess.move] = player_id;
 				out.writeObject(mess); // Tell the server.
 				mess = (GameMessage)in.readObject();
 
 				// Continue to the next state.
 				continue;
 			}
 
 			// (Determine an action) Create features based on the current board layout.
 			double max = 0;
 			int action = 0;
 			int def_do = -1;
 			int def_do2 = -1;
 			for (int x = 0; x < gameRules.numCols; x++)
 			{
 				// Create some variables.
 				FeatureExplorer[] ff = new FeatureExplorer[gameRules.numCols];
 				boolean[] ff_use = new boolean[gameRules.numCols];
 				int numFeatures = FeatureExplorer.getNumFeatures();
 				double[][] features = new double[gameRules.numCols][numFeatures];
 				double[] wx = new double[gameRules.numCols];
 				double[] sig = new double[gameRules.numCols];
 				double[] w = weights.getWeights();
 
 				// Reward variables.
 				double reward1 = 0.0f;
 				double reward2 = 0.0f;
 
 				// Generate a temporary board with this action placed in it.
 				int[][] temp_action_board = new int[gameRules.numRows][gameRules.numCols];
 				for (int c = 0; c < gameRules.numCols; c++)
 					for (int r = 0; r < gameRules.numRows; r++)
 						temp_action_board[r][c] = internal_board[r][c];
 				int r2 = 0;
 				for (r2 = 0; r2 < gameRules.numRows; r2++)
 				{
 					if (temp_action_board[r2][x] != 0)
 					{
 						temp_action_board[r2 - 1][x] = player_id;
 						break;
 					}
 				}
 				if (r2 == gameRules.numRows) temp_action_board[r2 - 1][x] = player_id;
 
 				// For each column that the opponent can place an action on, see what the max value will be. e.g. Play as virtual player.
 				double opponent_max = 0.0;
 				int opponent_action = 0;
 				for (int c = 0; c < gameRules.numCols; c++)
 				{
 					// Generate another temporary board with this action placed in it.
 					int[][] temp_action_board2 = new int[gameRules.numRows][gameRules.numCols];
 					for (int c2 = 0; c2 < gameRules.numCols; c2++)
 						for (r2 = 0; r2 < gameRules.numRows; r2++)
 							temp_action_board2[r2][c2] = temp_action_board[r2][c2];
 					r2 = 0;
 					for (r2 = 0; r2 < gameRules.numRows; r2++)
 					{
 						if (temp_action_board2[r2][c] != 0)
 						{
 							temp_action_board2[r2 - 1][c] = player_id % 2 + 1;
 							break;
 						}
 					}
 					if (r2 == gameRules.numRows) temp_action_board2[r2 - 1][c] = player_id % 2 + 1;
 
 					// Perform feature exploration on the internal board, for the action on column c.
 					ff[c] = new FeatureExplorer();
 					ff_use[c] = ff[c].initialize(temp_action_board, gameRules.numRows, gameRules.numCols, c, player_id % 2 + 1);
 					if (ff_use[c]) features[c] = ff[c].getFeatures();
 
 					// Debug.
 					if (PRINT_DEBUG) System.out.println("Action Column " + c + " Features:");
 					if (PRINT_DEBUG) printD(features[c]);
 
 					// For each of the features, compute the sum of the weights times the corresponding feature x.
 					for (int y = 0; y < numFeatures; y++)
 					{
 						if (ff_use[c])
 							wx[c] += ((double)features[c][y]) * w[y];
 						else
 							wx[c] = 0;
 					}
 
 					// Compute the sigmoid of this linear combination. Note that sig now stores the approximated Q(s, a) value.
 					if (ff_use[c])
 						sig[c] = sigmoid(wx[c]);
 					else
 						sig[c] = 0;
 
 					// Debug.
 					//if (PRINT_DEBUG)
 					//	System.out.printf("... and the weights for %d: wx[%d]: %f approx_qsa[%d]: %f\n", x, c, wx[c], c, sig[c]);
 
 					// Check if this is a winning position for this player.
 					if (checkWin(temp_action_board2, gameRules.numRows, gameRules.numCols, gameRules.numConnect, player_id % 2 + 1))
 					{
 						sig[c] = 1.0f;
 						def_do2 = c;
 					}
 
 					// If this is the first column, it is obviously the max. If it is any other column, and the Q(s, a) is bigger, use it.
 					if (c == 0)
 					{
 						opponent_max = sig[0];
 						opponent_action = 0;
 					}
 					else if (sig[c] > opponent_max)
 					{
 						opponent_max = sig[c];
 						opponent_action = c;
 					}
 				}
 
 				// Just for clarity, high for the opponent is low for you.
 				opponent_max = 1.0f - opponent_max;
 
 				// Now that the opponents action is known, see what your action should be in the next round afterwards under the assumption the opponent takes this action.
 				for (r2 = 0; r2 < gameRules.numRows; r2++)
 				{
 					if (temp_action_board[r2][opponent_action] != 0)
 					{
 						temp_action_board[r2 - 1][opponent_action] = player_id % 2 + 1;
 						break;
 					}
 				}
 				if (r2 == gameRules.numRows) temp_action_board[r2 - 1][opponent_action] = player_id % 2 + 1;
 
 				// Recreate some variables.
 				ff = new FeatureExplorer[gameRules.numCols];
 				ff_use = new boolean[gameRules.numCols];
 				numFeatures = FeatureExplorer.getNumFeatures();
 				features = new double[gameRules.numCols][numFeatures];
 				wx = new double[gameRules.numCols];
 				sig = new double[gameRules.numCols];
 				w = weights.getWeights();
 
 				// For each column that you can take after your opponent moves, see what the max value will be. e.g. Play as virtual-virtual player.
 				double your_max = 0.0;
 				int your_action = 0;
 				for (int c = 0; c < gameRules.numCols; c++)
 				{
 					// Generate another temporary board with this action placed in it.
 					int[][] temp_action_board2 = new int[gameRules.numRows][gameRules.numCols];
 					for (int c2 = 0; c2 < gameRules.numCols; c2++)
 						for (r2 = 0; r2 < gameRules.numRows; r2++)
 							temp_action_board2[r2][c2] = temp_action_board[r2][c2];
 					r2 = 0;
 					for (r2 = 0; r2 < gameRules.numRows; r2++)
 					{
 						if (temp_action_board2[r2][c] != 0)
 						{
 							temp_action_board2[r2 - 1][c] = player_id;
 							break;
 						}
 					}
 					if (r2 == gameRules.numRows) temp_action_board2[r2 - 1][c] = player_id;
 
 					// Perform feature exploration on the internal board, for the action on column c.
 					ff[c] = new FeatureExplorer();
 					ff_use[c] = ff[c].initialize(temp_action_board, gameRules.numRows, gameRules.numCols, c, player_id);
 					if (ff_use[c]) features[c] = ff[c].getFeatures();
 
 					// Debug.
 					if (PRINT_DEBUG) System.out.println("Action Column " + c + " Features:");
 					if (PRINT_DEBUG) printD(features[c]);
 
 					// For each of the features, compute the sum of the weights times the corresponding feature x.
 					for (int y = 0; y < numFeatures; y++)
 					{
 						if (ff_use[c])
 							wx[c] += ((double)features[c][y]) * w[y];
 						else
 							wx[c] = 0;
 					}
 
 					// Compute the sigmoid of this linear combination. Note that sig now stores the approximated Q(s, a) value.
 					if (ff_use[c])
 						sig[c] = sigmoid(wx[c]);
 					else
 						sig[c] = 0;
 
 					// Debug.
 					//if (PRINT_DEBUG)
 					//	System.out.printf("... and the weights for %d: wx[%d]: %f approx_qsa[%d]: %f\n", x, c, wx[c], c, sig[c]);
 
 					// Check if this is a winning position for this player.
 					if (checkWin(temp_action_board2, gameRules.numRows, gameRules.numCols, gameRules.numConnect, player_id))
 					{
 						sig[c] = 1.0f;
 					}
 
 					// If this is the first column, it is obviously the max. If it is any other column, and the Q(s, a) is bigger, use it.
 					if (c == 0)
 					{
 						your_max = sig[0];
 						your_action = 0;
 					}
 					else if (sig[c] > your_max)
 					{
 						your_max = sig[c];
 						your_action = c;
 					}
 				}
 
 				// Put the board back to the way it was at the beginning.
 				for (int c = 0; c < gameRules.numCols; c++)
 					for (int r = 0; r < gameRules.numRows; r++)
 						temp_action_board[r][c] = internal_board[r][c];
 				for (r2 = 0; r2 < gameRules.numRows; r2++)
 				{
 					if (temp_action_board[r2][your_action] != 0)
 					{
 						temp_action_board[r2 - 1][your_action] = player_id;
 						break;
 					}
 				}
 				if (r2 == gameRules.numRows) temp_action_board[r2 - 1][your_action] = player_id;
 
 				// Your max should be set to opposite.
 				your_max = 1.0f - your_max;
 
 				// Check if this player is in a winning position.
 				if (checkWin(temp_action_board, gameRules.numRows, gameRules.numCols, gameRules.numConnect, player_id))
 				{
 					reward1 = 1.0f;
 					your_max = 1.0f;
 					def_do = x;
 				}
 
 				// Take the opponents approximated Q(s, a), and use that to determine the max.
 				double check_result = reward1 + gamma * your_max;
 				if (x == 0)
 				{
 					max = check_result;
 					action = 0;
 				}
 				else if (check_result > max)
 				{
 					max = check_result;
 					action = x;
 				}
 
 				if (PRINT_DEBUG) System.out.printf("Approximated Q(s, %d) = %f\n", x, check_result);
 			}
 
 			// The Q(s, a) value is the max of all of these. (Not used, just for a note).
 			approx_qsa = max;
 			if (PRINT_DEBUG) System.out.println("----------------------->>> Decided to take action " + action);
 
 			// (Exploration function) Epsilon-greedy.
 			if(Math.random() > epsilon)
 				selected_column = action;
 			else
 			{
 				selected_column = (int)(Math.random() * gameRules.numCols);
 				for (int i = 0; i < 1000 && internal_board[0][selected_column] != 0; i++) selected_column = (int)(Math.random() * gameRules.numCols);
 				System.out.println("I'm exploring a bit! I choose action: " + selected_column);
 			}
 
 			// If the opponent will win, the Q(s, a) value should be set to 1 to block.
 			if (def_do != -1) selected_column = def_do;
 			else if (def_do2 != -1) selected_column = def_do2;
 
 			// (Compute previous board layout) Previous is equal to the old current.
 			for (int j = 0; j < FeatureExplorer.getNumFeatures(); j++) x_i[j] = x_ip1[j];
 
 			// (Compute Current board layout) Since this is after you have gone and your opponent has gone, calculate the ***current*** board layout.
 			FeatureExplorer ff_cur = new FeatureExplorer();
 			ff_cur.initialize(internal_board, gameRules.numRows, gameRules.numCols, -1, player_id);
 			x_ip1 = ff_cur.getFeatures();
 
 			// (Update weights) Do the update of the weight vector for the previous iteration.
 			if(beginning == false)
 			{
 				if (PRINT_DEBUG) System.out.println("Approximated Q(s, a) (Intermediate): " + approx_qsa + "\tplayer_id: " + player_id);
 				sarsa(0, weights, x_i, x_ip1);
 			}
 
 			// (Take action) Update the internal representation for where this player put his token. Then, send the action to the server.
 			int r = 0;
 			for (r = 0; r < gameRules.numRows; r++)
 			{
 				if (internal_board[r][selected_column] != 0)
 				{
 					internal_board[r - 1][selected_column] = player_id;
 					break;
 				}
 			}
 			if (r == gameRules.numRows) internal_board[r - 1][selected_column] = player_id;
 			mess.move = selected_column;
 			out.writeObject(mess);
 			mess = (GameMessage)in.readObject();
 
 			// The beginning has happened now.
 			beginning = false;
 		}
 
 		// Print out the final approximated Q(s, a) value and the player id.
 		System.out.println("Approximated Q(s, a): " + approx_qsa + "\tplayer_id: " + player_id);
 
 		// (Compute previous board layout) Previous is equal to the old current.
 		for (int j = 0; j < FeatureExplorer.getNumFeatures(); j++) x_i[j] = x_ip1[j];
 
 		// (Compute Current board layout) Since this is after you have gone and your opponent has gone, calculate the ***current*** board layout.
 		FeatureExplorer ff_cur = new FeatureExplorer();
 		ff_cur.initialize(internal_board, gameRules.numRows, gameRules.numCols, -1, player_id);
 		x_ip1 = ff_cur.getFeatures();
 
 		// (Final Weight Update) Do a final update based on win/loss.
 		if(mess.win == thePlayer)
 		{
 			sarsa(1, weights, x_i, x_ip1);	//reward 1 for win
 			System.out.println("MLPlayerAlphaTwo has won.");
 		}
 		else
 		{
 			sarsa(0, weights, x_i, x_ip1);	//reward 0 for loss
 			System.out.println("MLPlayerAlphaTwo has lost.");
 		}
 
 		// Save the weights.
 		weights.saveWeights();
 
 		// Close the socket.
 		sock.close();
 	}
 
 	private double sigmoid(double t)
 	{
 		return 1.0f / (1.0f + (double)Math.exp(-t));
 	}
 
 	private void sarsa(int reward, Weights weights, double x_i[], double[] x_ip1)
 	{
 		// Set some local variables.
 		double[] w = weights.getWeights();
 		double wx = 0.0f;
 		double sig = 0.0f;
 
 		// Compute y_i.
 		double wxp1 = 0.0f;
 		for (int j = 0; j < FeatureExplorer.getNumFeatures(); j++) wxp1 += w[j] * x_ip1[j];
 		double sigp1 = sigmoid(wxp1);
 		double y_i = reward + gamma * sigp1;
 
 		// Print out some debug if needed.
 		if (PRINT_DEBUG)
 		{
 			System.out.println("SARSA Weights:");
 			weights.printWeights();
 		}
 
 		// For each feature, compute the dot product of w and x.
 		for (int j = 0; j < FeatureExplorer.getNumFeatures(); j++) wx += w[j] * x_i[j];
 		if (PRINT_DEBUG)
 			System.out.println("\twx: " + wx);
 
 		// Compute the sigmoid of this.
 		sig = sigmoid(wx);
 		if (PRINT_DEBUG)
 			System.out.println("\tsigmoid(wx): " + sig);
 
 		// Perform eligibility traces.
		for (int e = 0; e < game_history_length; e++)
		{
 			// Compute the delta.
 			double rtp1 = 0.0f;
			if (e == game_history_length - 1 && reward == 1.0f) rtp1 = 1.0f;
			double delta = rtp1 + gamma * sig - game_history[e];
 
 			// Find eligibility.
			double eligibility = 1.0f;
			for (int asdf = 0; asdf < game_history_length - e; asdf++) eligibility *= gamma * lambda;
 
 			// Weight vector update.
 			for (int j = 0; j < FeatureExplorer.getNumFeatures(); j++)
				w[j] = w[j] + eta * eligibility * ((y_i - sig) * (sig) * (1 - sig) * x_i[j]);
		}
 
 		// Update game history.
 		game_history[game_history_length] = sig;
 		game_history_length++;
 
 		// Set the new weights.
 		weights.setWeights(w);
 		if (PRINT_DEBUG)
 		{
 			System.out.println("SARSA New Weights:");
 			weights.printWeights();
 		}
 	}
 
 	private boolean checkWin(int[][] theoretical_board, int num_rows, int num_cols, int num_connect, int p)
 	{
 		// Iterate over each position on the board. If the player p has 4 in a row in some direction, they won.
 		for (int r = 0; r < num_rows; r++)
 		{
 			for (int c = 0; c < num_cols; c++)
 			{
 				// If this cell is one we are looking for...
 				if (theoretical_board[r][c] == p)
 				{
 					// Check all the directions from this cell.
 					for (float theta = 0.0f; theta < 360.0f; theta += 45.0f)
 					{
 						// Temporary variable!
 						int counter = 0;
 
 						// Count each of the values that are on the "length vector" and correspond to the player p.
 						for (int length = 0; length < num_connect; length++)
 						{
 							// Temporary variable!
 							double hypo = 1.0f;
 
 							// Find the hypotenuse of the "triangle."
 							if ((int)(theta / 45.0f) % 2 == 1) hypo = Math.sqrt(2.0f);
 
 							// Compute end point.
 							int sauce_r = r - length * (int)Math.round(hypo * Math.sin((theta) * Math.PI / 180.0f));
 							int sauce_c = c + length * (int)Math.round(hypo * Math.cos((theta) * Math.PI / 180.0f));
 
 							// If this is out of bounds continue.
 							if (sauce_r < 0 || sauce_c < 0 || sauce_r >= num_rows || sauce_c >= num_cols) continue;
 
 							// If this point is valid and equals the player, add one to the counter.
 							if (theoretical_board[sauce_r][sauce_c] == p) counter++;
 						}
 
 						// If the counter is equal to the length (numConnect), then this is a winning board for the player p!
 						if (counter == num_connect) return true;
 					}
 				}
 			}
 		}
 
 		// No win was found, return false.
 		return false;
 	}
 
 	private void printD(double[] array)
 	{
 		for(int i = 0; i < array.length; i++)
 			System.out.println("\t" + array[i]);
 	}
 
 	// The main function.
 	public static void main(String[] args)
 	{
 		// If no argument is specified, throw an error.
 		if(args.length != 1)
 		{
 	    		System.out.println("Usage:\n java MPlayerAlphaTwo [1|2]");
 	    		System.exit(-1);
 		}
 
 		// Get the player.
 		int my_id = Integer.parseInt(args[0]);
 
 		// Set the player object.
 		Player p = null;
 		if (my_id == 1) p = Player.ONE;
 		else if (my_id == 2) p = Player.TWO;
 		else
 		{
 			System.out.println("Usage:\n java MPlayerAlphaTwo [1|2]");
 			System.exit(-1);
 		}
 
 		// Create the MLPlayer object, and begin play.
 		MLPlayerAlphaTwo me = new MLPlayerAlphaTwo(p);
 		try
 		{
 			me.play(my_id);
 		}
 		catch (IOException ioe)
 		{
 			ioe.printStackTrace();
 		}
 		catch (ClassNotFoundException cnfe)
 		{
 			cnfe.printStackTrace();
 		}
 	}
 }
 
