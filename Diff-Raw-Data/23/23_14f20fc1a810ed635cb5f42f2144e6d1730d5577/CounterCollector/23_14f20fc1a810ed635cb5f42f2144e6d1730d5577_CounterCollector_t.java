 public class CounterCollector
 {
     private int nb_counters = 0;
     private Counter counters[];
     
     
     public CounterCollector(int nb_counters)
     {
         this.counters = new Counter[nb_counters];
         
         if (0 < nb_counters) {
             for (int i = 0; i < nb_counters; i++) {
                 this.counters[i] = new Counter();
                 this.nb_counters++;
             }
         }
     }
     
     public CounterCollector(int nb_counters, int[] values)
     {
         this(nb_counters);
        
        if (values.length == nb_counters) {
            for (int i = 0; i < nb_counters; i++) {
                this.counters[i].setValue(values[i]);
            }
         }
     }
     
     public CounterCollector(int nb_counters, int[] start, int[] end, int[] step)
     {
         this(nb_counters);
        
        if (start.length == nb_counters
            && end.length == nb_counters
            && step.length == nb_counters
        ) {
            for (int i = 0; i < nb_counters; i++) {
                this.counters[i] = new Counter(start[i], end[i], step[i]);
            }
         }
     }
     
     public Counter getCounter(int index)
     {
        if (this.counters[index] instanceof Counter) {
            return this.counters[index];
         }
         return null;
     }
     
     public CounterCollector increment()
     {
         int i = 0;
         if (0 < this.nb_counters) {
             while (i < this.nb_counters) {
                 this.counters[i].increment();
                 if (this.counters[i].isEnded()) {
                     this.counters[i].reset();
                     i++;
                 } else {
                     break;
                 }
             }
         }
         return this;
     }
     
     public CounterCollector decrement()
     {
         int i = 0;
         if (0 < this.nb_counters) {
             while (i < this.nb_counters) {
                 this.counters[i].decrement();
                 if (this.counters[i].isEnded()) {
                     this.counters[i].reset();
                     i++;
                 } else {
                     break;
                 }
             }
         }
         return this;
     }
 }
