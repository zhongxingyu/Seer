 package org.starrynte.solver24;
 
 import java.util.Arrays;
 
 import android.os.AsyncTask;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class CalculateTask extends AsyncTask<int[], Object, Void>
 {
 	private final static double[] ITERATIONS = { 1, 8, 192, 7680, 430080, 30965760 };
 	private final static byte OPER_ADD = 0, OPER_SUB = 1, OPER_MUL = 2, OPER_DIV = 3;
 	TextView resultsView;
 	ProgressBar progressBar;
 
 	public CalculateTask(TextView resultsView, ProgressBar progressBar)
 	{
 		this.resultsView = resultsView;
 		this.progressBar = progressBar;
 	}
 
 	@Override
 	protected Void doInBackground(int[]... params)
 	{
 		int[] temp = params[0];
 		int nTemp = temp.length - 1;
 		if (nTemp == 0)
 		{
			publishProgress(1000, (temp[0] == 24) ? temp[0] + " = 24" : "No solutions found.");
 			return null;
 		}
 		double[] numbers = new double[temp.length];
 		for (int i = 0; i <= nTemp; i++)
 			numbers[i] = temp[i];
 		Arrays.sort(numbers);
 
 		boolean foundSolution = false;
 		int counter = 0;
 		do
 		{
 			int dyck = (1 << (2 * nTemp - 1)) | (((1 << (nTemp - 1)) - 1) << (nTemp - 1)); //1 0 1^(t-1) 0^(t-1), t = nNumbers - 1
 			do
 			{
 				byte[] operations = new byte[nTemp];
 				do
 				{
 					double[] stack = new double[nTemp + 1];
 					stack[0] = numbers[0];
 					int numberIndex = 1, operIndex = 0, stackIndex = 1;
 					for (int i = 31 - Integer.numberOfLeadingZeros(dyck); i >= 0; i--)
 					{
 						if ((dyck & (1 << i)) == 0)
 						{
 							double a = stack[stackIndex -= 2], b = stack[stackIndex + 1];
 							switch (operations[operIndex++])
 							{
 							case OPER_ADD:
 								stack[stackIndex++] = a + b;
 								break;
 							case OPER_SUB:
 								stack[stackIndex++] = a - b;
 								break;
 							case OPER_MUL:
 								stack[stackIndex++] = a * b;
 								break;
 							case OPER_DIV:
 								stack[stackIndex++] = a / b;
 								break;
 							}
 						} else
 						{
 							stack[stackIndex++] = numbers[numberIndex++];
 						}
 					}
 					if (Math.abs(stack[0] - 24) < 1E-4)
 					{
 						publishProgress((int) (1000 * counter / ITERATIONS[nTemp]), getString(numbers, dyck, operations));
 						foundSolution = true;
 					}
 
 					counter++;
 					if (counter % 100 == 0)
 						publishProgress((int) (1000 * counter / ITERATIONS[nTemp]));
 				} while (nextOperations(operations));
 			} while ((dyck = nextDyck(dyck)) != 0);
 		} while (nextPermutation(numbers));
 
 		if (foundSolution)
 			publishProgress(1000);
 		else
 			publishProgress(1000, "No solutions found.");
 		return null;
 	}
 
 	@Override
 	//values[0] is 10 * % done (Integer)
 	//values[1] (if present) is new solution found (String)
 	protected void onProgressUpdate(Object... values)
 	{
 		progressBar.setProgress((Integer) values[0]);
 		if (values.length > 1)
 		{
 			resultsView.append(((String) values[1]) + "\n");
 		}
 	}
 
 	//algorithm taken from wikipedia
 	private boolean nextPermutation(double[] numbers)
 	{
 		//step 1
 		int k = -1;
 		for (int i = numbers.length - 2; i >= 0; i--)
 		{
 			if (numbers[i] < numbers[i + 1])
 			{
 				k = i;
 				break;
 			}
 		}
 		if (k == -1)
 			return false;
 
 		//step 2
 		int l = numbers.length - 1;
 		while (numbers[k] >= numbers[l])
 			l--;
 
 		//step 3
 		swap(numbers, k, l);
 
 		//step 4
 		for (int i = 1; i <= (numbers.length - (k + 1)) / 2; i++)
 		{
 			swap(numbers, i + k, numbers.length - i);
 		}
 		return true;
 	}
 
 	private void swap(double[] numbers, int a, int b)
 	{
 		double temp = numbers[a];
 		numbers[a] = numbers[b];
 		numbers[b] = temp;
 	}
 
 	//algorithm taken from Table 1 of http://webhome.csc.uvic.ca/~haron/coolKat.pdf
 	private int nextDyck(int dyck)
 	{
 		int i = 0, j = 0;
 		for (int bit = Integer.highestOneBit(dyck); bit > 0; bit >>>= 1)
 		{
 			if (j == 0)
 			{
 				if ((dyck & bit) == bit)
 					i++;
 				else
 					j++;
 			} else
 			{
 				if ((dyck & bit) == 0)
 				{
 					j++;
 				} else
 				{
 					int temp = 31 - Integer.numberOfLeadingZeros(dyck);
 					if ((dyck & (bit >>>= 1)) == bit || (i == j))
 					{
 						return swap(dyck, temp - i, temp - (i + j));
 					} else
 					{
 						return swap(swap(dyck, temp - 1, temp - i), temp - (i + j), temp - (i + j + 1));
 					}
 				}
 			}
 		}
 		return 0;
 	}
 
 	//algorithm taken from http://graphics.stanford.edu/~seander/bithacks.html#SwappingBitsXOR
 	private int swap(int dyck, int a, int b)
 	{
 		int x = ((dyck >> a) ^ (dyck >> b)) & 1;
 		return dyck ^ ((x << a) | (x << b));
 	}
 
 	private boolean nextOperations(byte[] operations)
 	{
 		for (int i = 0; i < operations.length; i++)
 		{
 			if (++operations[i] <= OPER_DIV)
 			{
 				return true;
 			} else
 			{
 				operations[i] = 0;
 			}
 		}
 		return false;
 	}
 
 	private String getString(double[] numbers, int dyck, byte[] operations)
 	{
 		String[] stack = new String[numbers.length];
 		stack[0] = String.valueOf((int) numbers[0]);
 		int numberIndex = 1, operIndex = 0, stackIndex = 1;
 		for (int i = 31 - Integer.numberOfLeadingZeros(dyck); i >= 0; i--)
 		{
 			if ((dyck & (1 << i)) == 0)
 			{
 				String a = stack[stackIndex -= 2], b = stack[stackIndex + 1];
 				switch (operations[operIndex++])
 				{
 				case OPER_ADD:
 					stack[stackIndex++] = "(" + a + " + " + b + ")";
 					break;
 				case OPER_SUB:
 					stack[stackIndex++] = "(" + a + " - " + b + ")";
 					break;
 				case OPER_MUL:
 					stack[stackIndex++] = "(" + a + " * " + b + ")";
 					break;
 				case OPER_DIV:
 					stack[stackIndex++] = "(" + a + " / " + b + ")";
 					break;
 				}
 			} else
 			{
 				stack[stackIndex++] = String.valueOf((int) numbers[numberIndex++]);
 			}
 		}
 		return stack[0].substring(1, stack[0].length() - 1);
 	}
 
 }
