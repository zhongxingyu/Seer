 //вот бы еще и разобраться в нем
 //TODO razobrat's9!
package javaapplication3;
import java.util.Scanner;
public class JavaApplication3 {
    public static void main(String[] args) {
        Scanner scanner= new Scanner(System.in);
        int n = scanner.nextInt();
        int glavnaya_diagonal,vtorostep_diagonal;
        glavnaya_diagonal=vtorostep_diagonal=0;
        int [][] a;
        a = new int[n][n];
        for (int i=0;i<n;i++){
            for (int j=0;j<n;j++){
                a[i][j]=scanner.nextInt();
            }
        }
        for (int i=0;i<n;i++){
            glavnaya_diagonal+=a[i][i]*a[i][i];
        }
        for (int i=n-1;i>=0;i--){
            vtorostep_diagonal+=a[i][n-1-i]*a[i][n-1-i];
        }
        System.out.println(glavnaya_diagonal-vtorostep_diagonal);
    }
}
