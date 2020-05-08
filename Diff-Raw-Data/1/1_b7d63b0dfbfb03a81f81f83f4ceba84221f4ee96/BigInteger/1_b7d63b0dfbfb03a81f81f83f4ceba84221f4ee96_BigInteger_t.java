 import java.io.*; // 입력을 받기 위해 이 라이브러리가 필요하다.
 
 public class BigInteger
 {
   public int value;
 
   private int[] data;
 
   public BigInteger(int value)
   {
     this.value = value;
     this.data = new int[201];
     Initialize();
     GetFromInt(value);
   }
 
   public boolean IsEqual(BigInteger rhs)
   {
       for(int i=0; i<data.length; i++)
       {
           if (data[i] != rhs.data[i])
           {
               return false;
           }
       }
       return true;
   }
 
   private void Initialize()
   {
       for (int i=0; i < data.length; i++)
       {
           data[i] = 0;
       }
   }
 
   private void GetFromInt(int intVal)
   {
       int index = 0;
       while(intVal > 0)
       {
           data[index] = intVal % 10;
           intVal = intVal / 10;
           index += 1;
       }
   }
 
   public void Push(int lastDigit)
   {
       for (int i=data.length-1; i>0; i-=1)
       {
           data[i] = data[i-1];
       }
       data[0] = lastDigit;
 
       value = value * 10 + lastDigit;
   }
 
   public BigInteger Add(BigInteger rhs)
   {
       BigInteger newBigInt = new BigInteger(0);
       for (int i=0; i<data.length-1; i++)
       {
           newBigInt.data[i] += (data[i] + rhs.data[i]) % 10;
           newBigInt.data[i+1] += (data[i] + rhs.data[i]) / 10;
       }
      newBigInt.value = value + rhs.value;
       return newBigInt;
   }
 
   public String toString()
   {
     int firstDigitPos = data.length - 1;
     for (; firstDigitPos >= 0; firstDigitPos-=1)
     {
         if (data[firstDigitPos] != 0)
         {
             break;
         }
     }
 
     StringBuilder sb = new StringBuilder();
 
     for (int i=firstDigitPos; i >= 0; i-=1)
     {
         sb.append(data[i]);
     }
 
     return sb.toString();
   }
 
   public int GetInt()
   {
     return value;
   }
 
   public BigInteger GetClone()
   {
     return new BigInteger(value);
   }
 
 	public static void main(String args[])
 	{
 		// 입력을 받기 위한 작업이다.
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
 		// quit가 나올 때 까지 입력을 받아야 하므로
 		// while(true) 문을 사용하여 계속 반복한다.
 		while (true)
 		{
 			try   // try 와 catch 문은 오류 발생을 감지한다.
 			{
 				String input = br.readLine();	// 한 줄을 입력 받아 input 문자열에 저장
 
 				if (input.compareTo("quit") == 0)
 				{
 					// quit 라고 입력받았을 경우 종료한다.
 					// 종료하려면 while 문을 빠져나와야 하므로 break를 사용한다.
 					break;
 				}
 
 				// quit가 아니라면 식을 계산해야 한다.
 				calculate(input);
 			}
 			catch (Exception e)
 			{
 				// 만약 try { } 안에서 오류가 발생했다면 이곳으로 오게 된다.
 				// 이렇게 함으로써 입력을 이상하게 했을 경우 발생하는 오류를 방지한다.
 				System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
 			}
 		}
 	}
 
 	// 이 함수에서 입력받은 input 문자열을 이용하여 계산을 실시한다.
 	// 위의 main 함수를 완벽하게 이해한 뒤 이 함수의 내용을 자유롭게 구성해보라.
 	private static void calculate(String input)
 	{
 		// 아래 코드는 입력을 정상적으로 받는지 테스트하는 부분이다.
 
 		// TODO : Main 함수를 이해했다면 아래 문장을 삭제하고 구현해라.
 		System.out.println("<< calculate 함수에서 " + input + "을 계산할 예정입니다 >>");
 	}
 }
