 package com.jclark.microxml.tree;
 
 import java.util.Arrays;
 
 /**
  * Maps from offsets in the source to line-number/column-number
  */
 class LineMap {
     private final String url;
 
     private int[] lineStartOffset;
     private int count;
 
     LineMap(String url) {
         this.url = url;
         lineStartOffset = new int[8];
         count = 0;
     }
 
     // offset is of character following line ending
     // 0 is automatically a line start
     // line start indices must be strictly increasing
     void addLineStart(int offset) {
         if (offset <= 0
            || (count > 0 && lineStartOffset[count] >= offset))
             throw new IllegalArgumentException();
         if (count == lineStartOffset.length)
             lineStartOffset = Arrays.copyOf(lineStartOffset, lineStartOffset.length * 2);
         lineStartOffset[count++] = offset;
     }
 
     LinePosition get(int target) {
         if (target < 0)
             return LinePosition.VOID;
         // find the last i such that lineStartOffset[i] <= target
         int lo = 0;
         int hi = count - 1;
         while (lo <= hi) {
             // invariant: i if it exists is between lo and hi inclusive
             // we add 1 so that mid is always > lo (if lo != hi)
             int mid = lo + (hi + 1 - lo)/2;
             int midVal = lineStartOffset[mid];
             if (hi == lo) {
                 if (midVal > target)
                     break;
                 return new LinePosition(lo + 2, 1 + target - midVal);
             }
             assert mid > lo;
             assert mid <= hi;
             if (midVal <= target)
                 lo = mid;
             else
                 hi = mid - 1;
         }
         return new LinePosition(1, target + 1);
     }
 
 
     Location getLocation(final int startIndex, final int endIndex) {
        return new AbstractLocation() {
            @Override
            public String getURL() {
                return url;
            }
 
            @Override
            public long getStartIndex() {
                return startIndex;
            }
 
            @Override
            public long getEndIndex() {
                return endIndex;
            }
 
            @Override
            public LinePosition getStartLinePosition() {
                return get(startIndex);
            }
 
            @Override
            public LinePosition getEndLinePosition() {
                return get(endIndex);
            }
        };
     }
 }
