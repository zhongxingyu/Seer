 package ch01.ex02;
 
 /**
  * ��K���1.2�FHelloWorld�̈ꕔ��ύX���āA�ǂ̂悤�ȃG���[���������邩���ׂ�B
  */
 
private class HelloWorld {
 	/** HelloWorld�N���X��private�錾�ɂ���ƈȉ��̃G���[�������B
 	 * "Illegal modifier for the class HelloWorld; only public, abstract & final are permitted"
 	 * 
 	 * ���AHelloWorld�N���X��public�錾�ɖ߂��Amain���\�b�h��private�錾����ƁA�R���p�C���͐�������B
 	 */
 	public static void main (String[] args) {
 		System.out.println("Hello, world");
 	}
 }
