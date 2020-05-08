 package ch04.ex04_05;
 
class TreeWalker<E, T> {
   Strategy strategy;
  E root;
   
  /** ダミー */
  boolean search(T target) {
    // Strategy オブジェクトに応じて、深さ優先や幅優先を切り替える
    return true;
  }
 }
